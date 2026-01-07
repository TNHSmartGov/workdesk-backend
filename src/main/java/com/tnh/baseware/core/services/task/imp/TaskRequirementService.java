package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.constants.MessageConstant;
import com.tnh.baseware.core.dtos.task.TaskRequirementDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.events.factory.TaskActivityEventFactory;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.forms.task.AssignRequirementForm;
import com.tnh.baseware.core.forms.task.TaskRequirementEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskRequirementMapper;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.repositories.task.ITaskRequirementRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskCommandService;
import com.tnh.baseware.core.services.task.ITaskRequirementService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskRequirementService extends GenericService<TaskRequirement, TaskRequirementEditorForm, TaskRequirementDTO, ITaskRequirementRepository, ITaskRequirementMapper, UUID> implements ITaskRequirementService {
    ITaskRepository taskRepository;
    ITaskMemberRepository taskMemberRepository;
    IUserRepository userRepository;
    ITaskCommandService taskCommandService;
    ApplicationEventPublisher eventPublisher;

    public TaskRequirementService(ITaskRequirementRepository repository,
                                  ITaskRequirementMapper mapper,
                                  MessageService messageService,
                                  ITaskRepository taskRepository,
                                  IUserRepository userRepository,
                                  ITaskMemberRepository taskMemberRepository,
                                  ITaskCommandService taskCommandService,
                                  ApplicationEventPublisher eventPublisher) {
        super(repository, mapper, messageService, TaskRequirement.class);
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.taskMemberRepository = taskMemberRepository;
        this.taskCommandService = taskCommandService;
    }

    @Override
    @Transactional
    public TaskRequirementDTO create(UUID taskId, TaskRequirementEditorForm form) {
        Task task = getTask(taskId);

        if (form.getAssigneeId() != null) {
            validateAssigneeIsMember(taskId, form.getAssigneeId());
        }

        Integer maxOrder = repository.findMaxSortOrderByTaskId(taskId).orElse(0);

        TaskRequirement requirement = TaskRequirement.builder()
                .task(task)
                .content(form.getContent())
                .weight(form.getWeight() != null ? form.getWeight() : 1)
                .sortOrder(form.getSortOrder() != null ? form.getSortOrder() : maxOrder + 1)
                .isCompleted(false)
                .build();

        if (form.getAssigneeId() != null) {
            User assignee = userRepository.findById(form.getAssigneeId())
                    .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found")));
            requirement.setAssignee(assignee);
        }

        TaskRequirement saved = repository.save(requirement);

        eventPublisher.publishEvent(
                TaskActivityEventFactory.requirementAdded(
                        task,
                        getCurrentUser().getUsername(),
                        form.getContent()
                )
        );

        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional
    public TaskRequirementDTO update(UUID taskId, UUID requirementId, TaskRequirementEditorForm form) {
        TaskRequirement requirement = getRequirement(taskId, requirementId);

        if (form.getAssigneeId() != null) {
            validateAssigneeIsMember(taskId, form.getAssigneeId());
            User assignee = userRepository.findById(form.getAssigneeId())
                    .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found")));
            requirement.setAssignee(assignee);
        }

        requirement.setContent(form.getContent());
        requirement.setWeight(form.getWeight() != null ? form.getWeight() : requirement.getWeight());
        if (form.getSortOrder() != null) {
            requirement.setSortOrder(form.getSortOrder());
        }

        TaskRequirement saved = repository.save(requirement);

        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional
    public void delete(UUID taskId, UUID requirementId) {
        TaskRequirement requirement = getRequirement(taskId, requirementId);
        repository.delete(requirement);
        taskCommandService.calculateProgressFromRequirements(taskId);
    }

    @Override
    @Transactional
    public TaskRequirementDTO assignToMember(UUID taskId, UUID requirementId, AssignRequirementForm form) {
        TaskRequirement requirement = getRequirement(taskId, requirementId);

        validateAssigneeIsMember(taskId, form.getAssigneeId());

        User oldAssignee = requirement.getAssignee();
        User newAssignee = userRepository.findById(form.getAssigneeId())
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found")));

        requirement.setAssignee(newAssignee);
        TaskRequirement saved = repository.save(requirement);

        eventPublisher.publishEvent(
                TaskActivityEventFactory.requirementAssigned(
                        requirement.getTask(),
                        getCurrentUser().getUsername(),
                        requirement.getContent(),
                        oldAssignee,
                        newAssignee
                )
        );

        if (requirement.getIsCompleted()) {
            taskCommandService.calculateProgressFromRequirements(taskId);
        }

        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional
    public void toggleComplete(UUID taskId, UUID requirementId) {
        TaskRequirement requirement = getRequirement(taskId, requirementId);

        boolean newValue = !requirement.getIsCompleted();
        requirement.setIsCompleted(newValue);

        repository.save(requirement);

        eventPublisher.publishEvent(
                TaskActivityEventFactory.requirementCompleted(
                        requirement.getTask(),
                        getCurrentUser().getUsername(),
                        requirement.getContent(),
                        newValue
                )
        );

        taskCommandService.calculateProgressFromRequirements(taskId);
    }

    @Override
    public List<TaskRequirementDTO> getByTaskId(UUID taskId) {
        return repository.findByTaskIdOrderBySortOrder(taskId).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    private Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_NOT_FOUND));
    }

    private TaskRequirement getRequirement(UUID taskId, UUID requirementId) {
        TaskRequirement requirement = repository.findById(requirementId)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("requirement.not.found")));

        if (!requirement.getTask().getId().equals(taskId)) {
            throw new BWCValidationException(messageService.getMessage("requirement.not.belong.to.task"));
        }

        return requirement;
    }

    private void validateAssigneeIsMember(UUID taskId, UUID userId) {
        if (!taskMemberRepository.existsByTaskIdAndUserId(taskId, userId)) {
            throw new BWCValidationException(messageService.getMessage("user.not.task.member"));
        }
    }
}
