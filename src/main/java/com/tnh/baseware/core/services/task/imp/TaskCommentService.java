package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskCommentDTO;
import com.tnh.baseware.core.entities.task.TaskComment;
import com.tnh.baseware.core.forms.task.TaskCommentEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskCommentMapper;
import com.tnh.baseware.core.repositories.task.ITaskCommentRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskCommentService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import com.tnh.baseware.core.events.factory.TaskActivityEventFactory;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskCommentService extends
        GenericService<TaskComment, TaskCommentEditorForm, TaskCommentDTO, ITaskCommentRepository, ITaskCommentMapper, UUID>
        implements ITaskCommentService {

    ApplicationEventPublisher eventPublisher;

    public TaskCommentService(ITaskCommentRepository repository, ITaskCommentMapper mapper,
            MessageService messageService, ApplicationEventPublisher eventPublisher) {
        super(repository, mapper, messageService, TaskComment.class);
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public TaskCommentDTO create(TaskCommentEditorForm form) {
        if (form.getParentCommentId() != null) {
            var parent = repository.findById(form.getParentCommentId())
                    .orElseThrow(() -> new BWCNotFoundException(
                            messageService.getMessage("comment.not.found")));

            if (parent.getParentComment() != null) {
                // Determine if we should error or flatten. User requested "max 2 levels".
                // Often this implies strict validation.
                throw new BWCBusinessException(
                        messageService.getMessage("task.comment.max.depth.exceeded"));
            }
        }

        TaskCommentDTO dto = super.create(form);
        TaskComment savedComment = repository.findById(dto.getId()).orElse(null);

        if (savedComment != null) {
            eventPublisher.publishEvent(
                    TaskActivityEventFactory.commentAdded(
                            savedComment.getTask(),
                            savedComment.getCreatedBy(),
                            savedComment.getContent()));
        }

        return dto;
    }

}
