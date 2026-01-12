package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.constants.FieldChange;
import com.tnh.baseware.core.constants.MessageConstant;
import com.tnh.baseware.core.constants.TaskSnapshot;
import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.dtos.task.UserTaskPermissionDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskList;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.enums.project.ProjectMemberRole;
import com.tnh.baseware.core.enums.project.ProjectType;
import com.tnh.baseware.core.enums.task.*;
import com.tnh.baseware.core.events.factory.TaskActivityEventFactory;
import com.tnh.baseware.core.exceptions.BWCAccessDeniedException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskMapper;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.repositories.task.ITaskRequirementRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectService;
import com.tnh.baseware.core.services.task.ITaskCommandService;
import com.tnh.baseware.core.utils.DiffUtil;
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
public class TaskCommandService extends GenericService<Task, TaskEditorForm, TaskDTO, ITaskRepository, ITaskMapper, UUID> implements ITaskCommandService {
    ITaskListRepository taskListRepository;
    ITaskMemberRepository taskMemberRepository;
    IProjectService projectService;
    ITaskRequirementRepository taskRequirementRepository;
    ApplicationEventPublisher eventPublisher;

    public TaskCommandService(ITaskRepository repository,
                              ITaskMapper mapper,
                              MessageService messageService,
                              ITaskListRepository taskListRepository,
                              ITaskMemberRepository taskMemberRepository,
                              ITaskRequirementRepository taskRequirementRepository,
                              IProjectService projectService,
                              ApplicationEventPublisher eventPublisher) {
        super(repository, mapper, messageService, Task.class);
        this.taskListRepository = taskListRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.taskRequirementRepository = taskRequirementRepository;
        this.projectService = projectService;
        this.eventPublisher = eventPublisher;
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
                    .orElseThrow(() -> new BWCValidationException(MessageConstant.ERROR_CREATE_PROJECT_WITH_DEFAULT_TASK_LIST));
        }

        Task task = mapper.formToEntity(form);
        task.setTaskList(taskList);
        task.setProject(taskList.getProject());
        task.setStatus(TaskStatus.TODO);

        Task savedTask = repository.save(task);

        if (task.getProject().getType() == ProjectType.PERSONAL) {
            taskMemberRepository.save(TaskMember.builder()
                    .user(getCurrentUser())
                    .task(savedTask)
                    .role(TaskMemberRole.ASSIGNEE)
                    .build());
        }

        eventPublisher.publishEvent(
                TaskActivityEventFactory.created(savedTask, getCurrentUser().getUsername())
        );

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

        mapper.formToEntity(form, task);
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
                        task.getStatus()
                )
        );
    }

    @Override
    @Transactional
    public void calculateProgressFromRequirements(UUID taskId) {
        Task task = repository.findById(taskId).orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_NOT_FOUND));

        List<TaskRequirement> requirements = taskRequirementRepository.findByTaskId(taskId);
        if (requirements.isEmpty()) return;

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
            updateTaskProgress(task);

            eventPublisher.publishEvent(
                    TaskActivityEventFactory.systemUpdated(
                            task,
                            "progress",
                            null,
                            task.getProgress()
                    )
            );
        }
    }

    @Override
    @Transactional
    public void updatePersonalProgress(UUID taskId, Integer progress) {
        if (progress < 0 || progress > 100) throw new BWCValidationException(MessageConstant.PROGRESS_VALIDATE);

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

        updateTaskProgress(member.getTask());

        eventPublisher.publishEvent(
                TaskActivityEventFactory.progressUpdated(
                        member.getTask(),
                        getCurrentUser().getUsername(),
                        oldProgress,
                        progress
                )
        );
    }

    private void start(Task task) {
        if (task.getStatus() != TaskStatus.TODO) {
            throw new BWCValidationException(MessageConstant.VALIDATE_START_ACTION);
        }

        if (task.getStartDate() == null) {
            task.setStartDate(Instant.now());
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
                            projectRole, taskRole, action)
            );
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

    private void updateTaskProgress(Task task) {
        List<TaskMember> members = taskMemberRepository.findByTaskId(task.getId());

        if (members.isEmpty()) return;

        double totalWeightedProgress = 0;
        double totalWeight = 0;

        for (TaskMember m : members) {
            if (m.getRole() == TaskMemberRole.ASSIGNEE) {
                int w = (m.getWeight() == null) ? 1 : m.getWeight();
                totalWeightedProgress += (m.getPersonalProgress() * w);
                totalWeight += w;
            }
        }

        int overallProgress = (totalWeight == 0) ? 0 : (int) (totalWeightedProgress / totalWeight);
        task.setProgress(overallProgress);

        if (overallProgress > 0 && task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }

        repository.save(task);
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
