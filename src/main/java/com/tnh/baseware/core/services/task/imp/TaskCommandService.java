package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.constants.FieldChange;
import com.tnh.baseware.core.constants.MessageConstant;
import com.tnh.baseware.core.constants.TaskSnapshot;
import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.dtos.task.UserTaskPermissionDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.task.*;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.project.ProjectMemberRole;
import com.tnh.baseware.core.enums.project.ProjectType;
import com.tnh.baseware.core.enums.task.*;
import com.tnh.baseware.core.events.factory.TaskActivityEventFactory;
import com.tnh.baseware.core.exceptions.BWCAccessDeniedException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.forms.task.CreateTaskReportForm;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.mappers.task.ITaskMapper;
import com.tnh.baseware.core.repositories.task.ITaskCategoryRepository;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.repositories.task.ITaskRequirementRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectService;
import com.tnh.baseware.core.services.task.ITaskCommandService;
import com.tnh.baseware.core.utils.DiffUtil;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.repositories.task.ITaskAttachmentRepository;
import com.tnh.baseware.core.repositories.task.ITaskCommentAttachmentRepository;
import com.tnh.baseware.core.repositories.task.ITaskCommentRepository;
import com.tnh.baseware.core.repositories.task.ITaskDependencyRepository;
import com.tnh.baseware.core.repositories.task.ITaskDocumentRepository;
import com.tnh.baseware.core.repositories.doc.IFileDocumentRepository;
import com.tnh.baseware.core.entities.doc.FileDocument;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskCommandService
        extends GenericService<Task, TaskEditorForm, TaskDTO, ITaskRepository, ITaskMapper, UUID>
        implements ITaskCommandService {
    ITaskListRepository taskListRepository;
    ITaskCategoryRepository taskCategoryRepository;
    ITaskMemberRepository taskMemberRepository;
    IProjectService projectService;
    ITaskRequirementRepository taskRequirementRepository;
    GenericEntityFetcher fetcher;
    ApplicationEventPublisher eventPublisher;

    ITaskCommentAttachmentRepository taskCommentAttachmentRepository;
    ITaskCommentRepository taskCommentRepository;
    ITaskAttachmentRepository taskAttachmentRepository;
    ITaskActivityLogRepository taskActivityLogRepository;
    ITaskDocumentRepository taskDocumentRepository;
    ITaskDependencyRepository taskDependencyRepository;
    IFileDocumentRepository fileDocumentRepository;

    public TaskCommandService(ITaskRepository repository,
            ITaskMapper mapper,
            MessageService messageService,
            ITaskListRepository taskListRepository,
            ITaskCategoryRepository taskCategoryRepository,
            ITaskMemberRepository taskMemberRepository,
            ITaskRequirementRepository taskRequirementRepository,
            IProjectService projectService,
            GenericEntityFetcher fetcher,
            ApplicationEventPublisher eventPublisher,
            ITaskCommentAttachmentRepository taskCommentAttachmentRepository,
            ITaskCommentRepository taskCommentRepository,
            ITaskAttachmentRepository taskAttachmentRepository,
            ITaskActivityLogRepository taskActivityLogRepository,
            ITaskDocumentRepository taskDocumentRepository,
            ITaskDependencyRepository taskDependencyRepository,
            IFileDocumentRepository fileDocumentRepository) {
        super(repository, mapper, messageService, Task.class);
        this.taskListRepository = taskListRepository;
        this.taskCategoryRepository = taskCategoryRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.taskRequirementRepository = taskRequirementRepository;
        this.projectService = projectService;
        this.fetcher = fetcher;
        this.eventPublisher = eventPublisher;
        this.taskCommentAttachmentRepository = taskCommentAttachmentRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.taskActivityLogRepository = taskActivityLogRepository;
        this.taskDocumentRepository = taskDocumentRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.fileDocumentRepository = fileDocumentRepository;
    }

    @Override
    @Transactional
    public TaskDTO create(TaskEditorForm form) {
        validateDates(form);
        TaskList taskList;

        if (form.getTaskListId() != null) {
            taskList = taskListRepository.findById(form.getTaskListId())
                    .orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_LIST_NOT_FOUND));
        } else {
            Project personalProject = projectService.getOrCreatePersonalProject(getCurrentUser().getId());
            taskList = taskListRepository.findDefaultByProjectId(personalProject.getId())
                    .orElseThrow(() -> new BWCValidationException(
                            MessageConstant.ERROR_CREATE_PROJECT_WITH_DEFAULT_TASK_LIST));
        }

        Task task = mapper.formToEntity(form);
        task.setTaskList(taskList);
        task.setProject(taskList.getProject());
        task.setStatus(TaskStatus.TODO);
        if (form.getTaskCategoryId() != null) {
            task.setTaskCategory(taskCategoryRepository.findById(form.getTaskCategoryId())
                    .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("task_category.not_found"))));
        }

        Task savedTask = repository.save(task);

        if (task.getProject().getType() == ProjectType.PERSONAL) {
            taskMemberRepository.save(TaskMember.builder()
                    .user(getCurrentUser())
                    .task(savedTask)
                    .status(MemberStatus.ASSIGNED)
                    .role(TaskMemberRole.ASSIGNEE)
                    .build());
        } else {
            var taskMember = TaskMember.builder()
                    .task(task)
                    .user(getCurrentUser())
                    .role(TaskMemberRole.OWNER)
                    .status(MemberStatus.ASSIGNED)
                    .build();
            taskMemberRepository.save(taskMember);
        }

        eventPublisher.publishEvent(
                TaskActivityEventFactory.created(savedTask, getCurrentUser().getUsername()));

        return mapper.entityToDTO(savedTask);
    }

    @Override
    @Transactional
    public TaskDTO update(UUID id, TaskEditorForm form) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_NOT_FOUND));

        if (form.getTaskListId() != null &&
                (task.getTaskList() == null || !form.getTaskListId().equals(task.getTaskList().getId()))) {
            TaskList newList = taskListRepository.findById(form.getTaskListId())
                    .orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_LIST_NOT_FOUND));
            task.setTaskList(newList);
        }

        TaskSnapshot before = TaskSnapshot.from(task);

        mapper.updateFromForm(form, task, fetcher, taskListRepository, taskCategoryRepository);
        Task saved = repository.save(task);

        TaskSnapshot after = TaskSnapshot.from(saved);

        List<FieldChange> changes = DiffUtil.diff(before, after);

        TaskActivityEventFactory
                .fieldUpdatedBatch(saved, getCurrentUser().getUsername(), changes)
                .forEach(eventPublisher::publishEvent);

        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional
    public void performAction(UUID id, TaskAction action) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_NOT_FOUND));

        validateAction(task, action, getCurrentUser().getId());

        String oldStatus = task.getStatus().toString();

        switch (action) {
            case START -> start(task);
            case COMPLETE -> complete(task);
            case APPROVE -> approve(task);
            case CANCEL -> cancel(task);
            default -> throw new BWCValidationException(MessageConstant.UNSUPPORTED_ACTION);
        }

        repository.save(task);

        eventPublisher.publishEvent(
                TaskActivityEventFactory.statusChanged(
                        task,
                        getCurrentUser().getUsername(),
                        TaskStatus.valueOf(oldStatus),
                        task.getStatus()));
    }

    @Override
    @Transactional
    public void calculateProgressFromRequirements(UUID taskId) {
        Task task = repository.findById(taskId)
                .orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_NOT_FOUND));

        List<TaskRequirement> requirements = taskRequirementRepository.findByTaskId(taskId);
        if (requirements.isEmpty())
            return;

        List<TaskMember> taskMembers = taskMemberRepository.findByTask(task);

        Map<UUID, TaskMember> memberMap = taskMembers.stream()
                .collect(Collectors.toMap(m -> m.getUser().getId(), m -> m));

        Map<UUID, List<TaskRequirement>> requirementsByAssignee = requirements.stream()
                .filter(r -> r.getAssignee() != null)
                .collect(Collectors.groupingBy(r -> r.getAssignee().getId()));

        List<TaskMember> membersToUpdate = new ArrayList<>();

        requirementsByAssignee.forEach((assigneeId, reqs) -> {
            TaskMember member = memberMap.get(assigneeId);

            if (member != null) {
                int totalWeight = reqs.stream()
                        .mapToInt(r -> r.getWeight() == null ? 1 : r.getWeight())
                        .sum();

                int completedWeight = reqs.stream()
                        .filter(TaskRequirement::getIsCompleted)
                        .mapToInt(r -> r.getWeight() == null ? 1 : r.getWeight())
                        .sum();

                int progress = (totalWeight == 0)
                        ? 0
                        : (int) Math.round((completedWeight * 100.0) / totalWeight);

                member.setPersonalProgress(progress);
                member.setStatus(calculateStatusFromProgress(progress));
                membersToUpdate.add(member);
            }
        });

        if (!membersToUpdate.isEmpty()) {
            taskMemberRepository.saveAll(membersToUpdate);
            updateTaskProgress(task, taskMembers);

            eventPublisher.publishEvent(
                    TaskActivityEventFactory.systemUpdated(
                            task,
                            "progress",
                            null,
                            task.getProgress()));
        }
    }

    @Override
    @Transactional
    public void updatePersonalProgress(UUID taskId, Integer progress) {
        if (progress < 0 || progress > 100)
            throw new BWCValidationException(MessageConstant.PROGRESS_VALIDATE);

        if (taskRequirementRepository.existsByTaskId(taskId)) {
            throw new BWCValidationException(MessageConstant.BLOCK_UPDATE_PROGRESS_MANUAL);
        }

        UUID userId = getCurrentUser().getId();
        TaskMember member = taskMemberRepository.findByTaskIdAndUserId(taskId, userId)
                .orElseThrow(() -> new BWCAccessDeniedException(MessageConstant.NOT_ASSIGNED_TO_TASK));

        Integer oldProgress = member.getPersonalProgress();

        member.setPersonalProgress(progress);
        member.setStatus(calculateStatusFromProgress(progress));

        taskMemberRepository.save(member);

        taskMemberRepository.save(member);

        List<TaskMember> allMembers = taskMemberRepository.findByTaskId(taskId);
        updateTaskProgress(member.getTask(), allMembers);

        eventPublisher.publishEvent(
                TaskActivityEventFactory.progressUpdated(
                        member.getTask(),
                        getCurrentUser().getUsername(),
                        oldProgress,
                        progress));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!Boolean.TRUE.equals(isUserSystem())) {
            throw new BWCAccessDeniedException(MessageConstant.NOT_ALLOW_PERFORM_ACTION);
        }

        Task task = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_NOT_FOUND));

        UUID taskId = task.getId();

        // Delete task comment attachments
        taskCommentAttachmentRepository.deleteAll(
                taskCommentAttachmentRepository.findAll().stream()
                        .filter(tca -> tca.getComment() != null
                                && taskId.equals(tca.getComment().getTask().getId()))
                        .toList());

        // Delete task comments
        taskCommentRepository.deleteAll(
                taskCommentRepository.findAll().stream()
                        .filter(tc -> taskId.equals(tc.getTask().getId()))
                        .toList());

        // Delete task attachments
        taskAttachmentRepository.deleteAll(
                taskAttachmentRepository.findAll().stream()
                        .filter(ta -> taskId.equals(ta.getTask().getId()))
                        .toList());

        // Delete task members
        taskMemberRepository.deleteAll(taskMemberRepository.findByTaskId(taskId));

        // Delete task activity logs
        taskActivityLogRepository.deleteAll(
                taskActivityLogRepository.findAll().stream()
                        .filter(tal -> taskId.equals(tal.getTask().getId()))
                        .toList());

        // Delete task documents
        taskDocumentRepository.deleteAll(taskDocumentRepository.findAllByTask_Id(taskId));

        // Delete task dependencies
        taskDependencyRepository.deleteAll(
                taskDependencyRepository.findAll().stream()
                        .filter(tdep -> taskId.equals(tdep.getFromTask().getId())
                                || taskId.equals(tdep.getToTask().getId()))
                        .toList());

        // Delete task requirements
        taskRequirementRepository.deleteAll(taskRequirementRepository.findByTaskId(taskId));

        // Delete the task itself
        repository.delete(task);
    }

    private void start(Task task) {
        if (task.getStatus() != TaskStatus.TODO) {
            throw new BWCValidationException(MessageConstant.VALIDATE_START_ACTION);
        }

        if (task.getStartDate() == null) {
            Instant now = Instant.now();
            if (task.getDueDate() != null && now.isAfter(task.getDueDate())) {
                throw new BWCValidationException(MessageConstant.VALIDATE_START_ACTION);
            }
            task.setStartDate(now);
        } else if (task.getDueDate() != null && task.getStartDate().isAfter(task.getDueDate())) {
            throw new BWCValidationException(MessageConstant.VALIDATE_START_ACTION);
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
    }

    private void complete(Task task) {
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new BWCValidationException(MessageConstant.VALIDATE_COMPLETE_ACTION);
        }
        if (task.getProject().getType() == ProjectType.PERSONAL) {
            task.setStatus(TaskStatus.DONE);
        } else {
            task.setStatus(TaskStatus.REVIEW);
        }
    }

    private void approve(Task task) {
        if (task.getStatus() != TaskStatus.REVIEW) {
            throw new BWCValidationException(MessageConstant.VALIDATE_APPROVE_ACTION);
        }
        task.setStatus(TaskStatus.DONE);
    }

    private void cancel(Task task) {
        if (task.getStatus() == TaskStatus.DONE) {
            throw new BWCValidationException(MessageConstant.VALIDATE_CANCEL_ACTION);
        }
        task.setStatus(TaskStatus.CANCELLED);
    }

    private void validateAction(Task task, TaskAction action, UUID userId) {
        if (task.getProject().getType() == ProjectType.PERSONAL) {
            if (!task.getCreatedBy().equalsIgnoreCase(userId.toString())) {
                throw new BWCAccessDeniedException(MessageConstant.NOT_ALLOW_PERFORM_ACTION);
            }
            return;
        }

        UserTaskPermissionDTO perms = repository.findUserPermissions(task.getId(), userId)
                .orElseThrow(() -> new BWCAccessDeniedException(MessageConstant.NOT_IN_PROJECT_TASK));

        ProjectMemberRole projectRole;
        try {
            projectRole = ProjectMemberRole.fromValue(perms.getProjectRole());
        } catch (Exception e) {
            throw new BWCAccessDeniedException(MessageConstant.INVALID_PROJECT_ROLE_CONFIG);
        }

        TaskMemberRole taskRole = null;
        if (perms.getTaskRole() != null) {
            try {
                taskRole = TaskMemberRole.valueOf(perms.getTaskRole());
            } catch (IllegalArgumentException ignored) {
            }
        }

        boolean isAllowed = checkIsAllowed(action, projectRole, taskRole);

        if (!isAllowed) {
            throw new BWCAccessDeniedException(
                    String.format("Your role [P:%s - T:%s] is not allowed to perform action %s",
                            projectRole, taskRole, action));
        }
    }

    private static boolean checkIsAllowed(TaskAction action, ProjectMemberRole projectRole, TaskMemberRole taskRole) {
        boolean isProjectAdmin = projectRole == ProjectMemberRole.OWNER
                || projectRole == ProjectMemberRole.MANAGER;
        boolean isTaskLead = taskRole == TaskMemberRole.LEAD;
        boolean isProjectAdminOrTaskLead = isProjectAdmin || isTaskLead;

        return switch (action) {
            case START, COMPLETE, CANCEL -> isProjectAdminOrTaskLead;
            case APPROVE -> isProjectAdminOrTaskLead || taskRole == TaskMemberRole.REVIEWER;
        };
    }

    @Override
    @Transactional
    public void reportProgress(UUID taskId, CreateTaskReportForm form) {
        Task task = repository.findById(taskId)
                .orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_NOT_FOUND));

        User currentUser = getCurrentUser();

        // 1. Create Comment (Type REPORT)
        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(currentUser)
                .content(form.getContent())
                .type(TaskCommentType.REPORT)
                .build();

        TaskComment savedComment = taskCommentRepository.save(comment);

        // 2. Handle Attachments (if any)
        if (form.getFileIds() != null && !form.getFileIds().isEmpty()) {
            List<TaskCommentAttachment> attachments = form.getFileIds().stream()
                    .map(fileId -> {
                        // Correctly fetch FileDocument entity
                        FileDocument file = fileDocumentRepository.findById(fileId)
                                .orElseThrow(
                                        () -> new BWCNotFoundException(messageService.getMessage("file.not.found")));

                        return TaskCommentAttachment.builder()
                                .comment(savedComment)
                                .file(file)
                                .uploader(currentUser)
                                .build();
                    })
                    .toList();
            taskCommentAttachmentRepository.saveAll(attachments);
        }

        // 3. Update Member Status/Progress
        // Optimization: Fetch ALL members query once
        List<TaskMember> allMembers = taskMemberRepository.findByTaskId(taskId);

        TaskMember member = allMembers.stream()
                .filter(m -> m.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new BWCAccessDeniedException(MessageConstant.NOT_ASSIGNED_TO_TASK));

        Integer oldProgress = member.getPersonalProgress(); // Capture old progress
        MemberStatus oldStatus = member.getStatus(); // Capture old status

        // STATUS IS MANDATORY (Source of Truth)
        member.setStatus(form.getStatus());

        // PROGRESS LOGIC
        if (form.getProgress() != null) {
            // Case A: User provided explicit progress -> Use it (if allowed)
            if (!taskRequirementRepository.existsByTaskId(taskId)) {
                if (form.getProgress() < 0 || form.getProgress() > 100)
                    throw new BWCValidationException(MessageConstant.PROGRESS_VALIDATE);

                member.setPersonalProgress(form.getProgress());
            }
        } else {
            // Case B: User did NOT provide progress -> Infer from Status (Smart Default)
            if (!taskRequirementRepository.existsByTaskId(taskId)) {
                if (form.getStatus() == MemberStatus.COMPLETED) {
                    member.setPersonalProgress(100);
                } else if (form.getStatus() == MemberStatus.ASSIGNED) {
                    member.setPersonalProgress(0);
                }
                // If IN_PROGRESS, keep old progress (do nothing)
            }
        }

        // Detect changes using Objects.equals
        boolean progressChanged = !Objects.equals(oldProgress, member.getPersonalProgress());
        boolean statusChanged = !Objects.equals(oldStatus, member.getStatus());

        taskMemberRepository.save(member);

        if (progressChanged) {
            // Use optimized update method passing the existing list
            updateTaskProgress(task, allMembers);

            eventPublisher.publishEvent(
                    TaskActivityEventFactory.progressUpdated(
                            member.getTask(),
                            currentUser.getUsername(),
                            oldProgress,
                            member.getPersonalProgress())); // Use new actual value
        }

        if (statusChanged) {
            eventPublisher.publishEvent(
                    TaskActivityEventFactory.memberStatusUpdated(
                            member.getTask(),
                            currentUser.getUsername(),
                            member.getUser(),
                            oldStatus.toString(),
                            member.getStatus().toString()));
        }
    }

    @Override
    @Transactional
    public void recalculateTaskProgress(UUID taskId) {
        Task task = repository.findById(taskId)
                .orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_NOT_FOUND));

        // Fetch members here and pass to update logic
        List<TaskMember> members = taskMemberRepository.findByTaskId(taskId);
        updateTaskProgress(task, members);
    }

    private void updateTaskProgress(Task task, List<TaskMember> members) {
        if (members.isEmpty())
            return;

        double totalWeightedProgress = 0;
        double totalWeight = 0;

        for (TaskMember m : members) {
            // Consider only ASSIGNEE and LEAD roles when calculating execution progress.
            if (m.getRole() == TaskMemberRole.ASSIGNEE || m.getRole() == TaskMemberRole.LEAD) {
                // Expanded to LEAD as they might have tasks too.
                int w = (m.getWeight() == null) ? 1 : m.getWeight();
                totalWeightedProgress += (m.getPersonalProgress() * w);
                totalWeight += w;
            }
        }

        int overallProgress = (totalWeight == 0) ? 0 : (int) Math.round(totalWeightedProgress / totalWeight);

        // If progress changed, save and log
        if (!Objects.equals(task.getProgress(), overallProgress)) {
            task.setProgress(overallProgress);

            if (overallProgress > 0 && task.getStatus() == TaskStatus.TODO) {
                task.setStatus(TaskStatus.IN_PROGRESS);
            }
            // Do not automatically mark the task as COMPLETED at 100% progress; completion
            // is handled by an explicit workflow.

            repository.save(task);

            eventPublisher.publishEvent(
                    TaskActivityEventFactory.systemUpdated(
                            task,
                            "progress",
                            null,
                            task.getProgress()));
        }
    }

    private MemberStatus calculateStatusFromProgress(Integer progress) {
        return switch (progress) {
            case 0 -> MemberStatus.ASSIGNED;
            case 100 -> MemberStatus.COMPLETED;
            default -> MemberStatus.IN_PROGRESS;
        };
    }

    private void validateDates(TaskEditorForm form) {
        if (form.getStartDate() != null && form.getDueDate() != null) {
            if (form.getStartDate().isAfter(form.getDueDate())) {
                throw new BWCValidationException("Start date must be before or equal to due date");
            }
        }
    }
}
