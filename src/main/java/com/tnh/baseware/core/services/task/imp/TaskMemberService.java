package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.constants.MessageConstant;
import com.tnh.baseware.core.dtos.task.TaskMemberDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.task.MemberStatus;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.events.factory.TaskActivityEventFactory;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.forms.task.TaskMemberEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskMemberMapper;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskMemberService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskMemberService extends GenericService<TaskMember, TaskMemberEditorForm, TaskMemberDTO, ITaskMemberRepository, ITaskMemberMapper, UUID> implements ITaskMemberService {
    ITaskRepository taskRepository;
    IUserRepository userRepository;
    ApplicationEventPublisher eventPublisher;

    public TaskMemberService(ITaskMemberRepository repository,
                             ITaskMemberMapper mapper,
                             MessageService messageService,
                             ITaskRepository taskRepository,
                             IUserRepository userRepository,
                             ApplicationEventPublisher eventPublisher) {
        super(repository, mapper, messageService, TaskMember.class);
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public TaskMemberDTO assignMember(UUID taskId, TaskMemberEditorForm form) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BWCNotFoundException(MessageConstant.TASK_NOT_FOUND));

        User user = userRepository.findById(form.getUserId())
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found")));

        if (repository.existsByTaskIdAndUserId(taskId, form.getUserId())) {
            throw new BWCValidationException(messageService.getMessage("user.already.assigned"));
        }

        if (form.getRole() == TaskMemberRole.LEAD &&
                repository.existsByTaskIdAndRole(taskId, TaskMemberRole.LEAD)) {
            throw new BWCValidationException(messageService.getMessage("task.already.has.lead"));
        }

        TaskMember member = TaskMember.builder()
                .task(task)
                .user(user)
                .role(form.getRole())
                .weight(form.getWeight() != null ? form.getWeight() : 1)
                .personalProgress(0)
                .status(MemberStatus.ASSIGNED)
                .build();

        TaskMember saved = repository.save(member);

        eventPublisher.publishEvent(
                TaskActivityEventFactory.memberAssigned(task, getCurrentUser().getUsername(), user)
        );

        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional
    public TaskMemberDTO updateMember(UUID taskId, UUID memberId, TaskMemberEditorForm form) {
        TaskMember member = repository.findById(memberId)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("task.member.not.found")));

        if (!member.getTask().getId().equals(taskId)) {
            throw new BWCValidationException(messageService.getMessage("member.not.belong.to.task"));
        }

        // Validate LEAD uniqueness
        if (form.getRole() == TaskMemberRole.LEAD &&
                member.getRole() != TaskMemberRole.LEAD &&
                repository.existsByTaskIdAndRole(taskId, TaskMemberRole.LEAD)) {
            throw new BWCValidationException(messageService.getMessage("task.already.has.lead"));
        }

        TaskMemberRole oldRole = member.getRole();
        member.setRole(form.getRole());
        member.setWeight(form.getWeight() != null ? form.getWeight() : member.getWeight());

        TaskMember saved = repository.save(member);

        if (oldRole != form.getRole()) {
            eventPublisher.publishEvent(
                    TaskActivityEventFactory.memberRoleChanged(
                            member.getTask(),
                            getCurrentUser().getUsername(),
                            member.getUser(),
                            oldRole,
                            form.getRole()
                    )
            );
        }

        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional
    public void removeMember(UUID taskId, UUID memberId) {
        TaskMember member = repository.findById(memberId)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("task.member.not.found")));

        if (!member.getTask().getId().equals(taskId)) {
            throw new BWCValidationException(messageService.getMessage("member.not.belong.to.task"));
        }

        repository.delete(member);

        eventPublisher.publishEvent(
                TaskActivityEventFactory.memberRemoved(
                        member.getTask(),
                        getCurrentUser().getUsername(),
                        member.getUser()
                )
        );
    }

    @Override
    public List<TaskMemberDTO> getTaskMembers(UUID taskId) {
        return repository.findByTaskId(taskId).stream()
                .map(mapper::entityToDTO)
                .toList();
    }
}
