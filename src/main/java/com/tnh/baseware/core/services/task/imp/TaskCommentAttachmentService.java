package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskCommentAttachmentDTO;
import com.tnh.baseware.core.entities.task.TaskCommentAttachment;
import com.tnh.baseware.core.forms.task.TaskCommentAttachmentEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskCommentAttachmentMapper;
import com.tnh.baseware.core.repositories.task.ITaskCommentAttachmentRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskCommentAttachmentService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskCommentAttachmentService extends GenericService<TaskCommentAttachment, TaskCommentAttachmentEditorForm, TaskCommentAttachmentDTO, ITaskCommentAttachmentRepository, ITaskCommentAttachmentMapper, UUID> implements ITaskCommentAttachmentService {

    public TaskCommentAttachmentService(ITaskCommentAttachmentRepository repository, ITaskCommentAttachmentMapper mapper, MessageService messageService) {
        super(repository, mapper, messageService, TaskCommentAttachment.class);
    }
}
