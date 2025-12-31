package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.dtos.task.UserTaskPermissionDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskList;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.enums.project.ProjectMemberRole;
import com.tnh.baseware.core.enums.project.ProjectType;
import com.tnh.baseware.core.enums.task.TaskAction;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.exceptions.BWCAccessDeniedException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskMapper;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectService;
import com.tnh.baseware.core.services.task.ITaskService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskCommandService extends GenericService<Task, TaskEditorForm, TaskDTO, ITaskRepository, ITaskMapper, UUID> implements ITaskService {
    ITaskListRepository taskListRepository;
    ITaskMemberRepository taskMemberRepository;
    IProjectService projectService;

    public TaskCommandService(ITaskRepository repository,
                              ITaskMapper mapper,
                              MessageService messageService,
                              ITaskListRepository taskListRepository,
                              ITaskMemberRepository taskMemberRepository,
                              IProjectService projectService) {
        super(repository, mapper, messageService, Task.class);
        this.taskListRepository = taskListRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.projectService = projectService;
    }

    @Override
    @Transactional
    public TaskDTO create(TaskEditorForm form) {
        TaskList taskList;

        if (form.getTaskListId() != null) {
            taskList = taskListRepository.findById(form.getTaskListId())
                    .orElseThrow(() -> new BWCNotFoundException("Task list not found"));
        } else {
            Project personalProject = projectService.getOrCreatePersonalProject(getCurrentUser().getId());
            taskList = taskListRepository.findDefaultByProjectId(personalProject.getId())
                    .orElseThrow(() -> new BWCValidationException("System error: Personal Project initialized without Default Task List"));
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

        return mapper.entityToDTO(savedTask);
    }

    @Override
    @Transactional
    public void performAction(UUID id, TaskAction action) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException("Task not found"));

        validateAction(task, action, getCurrentUser().getId());

        switch (action) {
            case START -> start(task);
            case COMPLETE -> complete(task);
            case APPROVE -> approve(task);
            case CANCEL -> cancel(task);
            default -> throw new IllegalStateException("Unsupported action");
        }
    }

    private void start(Task task) {
        if (task.getStatus() != TaskStatus.TODO) {
            throw new IllegalStateException("Only TODO task can be started");
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
    }

    private void complete(Task task) {
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS task can be completed");
        }
        if (task.getProject().getType() == ProjectType.PERSONAL) {
            task.setStatus(TaskStatus.DONE);
        } else {
            task.setStatus(TaskStatus.REVIEW);
        }
    }

    private void approve(Task task) {
        if (task.getStatus() != TaskStatus.REVIEW) {
            throw new IllegalStateException("Only reviewing task can be approved");
        }
        task.setStatus(TaskStatus.DONE);
    }

    private void cancel(Task task) {
        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalStateException("Completed task cannot be cancelled");
        }
        task.setStatus(TaskStatus.CANCELLED);
    }

    public void validateAction(Task task, TaskAction action, UUID userId) {
        if (task.getProject().getType() == ProjectType.PERSONAL) {
            if (!task.getCreatedBy().equalsIgnoreCase(userId.toString())) {
                throw new BWCAccessDeniedException("You are not allowed to perform actions on this task");
            }
            return;
        }

        UserTaskPermissionDTO perms = repository.findUserPermissions(task.getId(), userId)
                .orElseThrow(() -> new BWCAccessDeniedException("You are not a member of the project and task"));

        ProjectMemberRole projectRole;
        try {
            projectRole = ProjectMemberRole.fromValue(perms.getProjectRole());
        } catch (Exception e) {
            throw new BWCAccessDeniedException("Invalid Project Role configuration");
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
        boolean isProjectAdminOrTaskLead = projectRole == ProjectMemberRole.OWNER || projectRole == ProjectMemberRole.MANAGER || taskRole == TaskMemberRole.LEAD;

        if (action == TaskAction.CANCEL) {
            return isProjectAdminOrTaskLead || taskRole == TaskMemberRole.ASSIGNEE;
        }

        if (taskRole != null) {
            return switch (action) {
                case START, COMPLETE -> taskRole == TaskMemberRole.ASSIGNEE || isProjectAdminOrTaskLead;
                case APPROVE -> taskRole == TaskMemberRole.REVIEWER || isProjectAdminOrTaskLead;
                default -> false;
            };
        }

        return false;
    }
}
