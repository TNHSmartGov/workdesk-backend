package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskAttachmentDTO;
import com.tnh.baseware.core.entities.task.TaskAttachment;
import com.tnh.baseware.core.forms.task.TaskAttachmentEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskAttachmentMapper;
import com.tnh.baseware.core.repositories.task.ITaskAttachmentRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskAttachmentService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskAttachmentService extends GenericService<TaskAttachment, TaskAttachmentEditorForm, TaskAttachmentDTO, ITaskAttachmentRepository, ITaskAttachmentMapper, UUID> implements ITaskAttachmentService {

    public TaskAttachmentService(ITaskAttachmentRepository repository, ITaskAttachmentMapper mapper, MessageService messageService) {
        super(repository, mapper, messageService, TaskAttachment.class);
    }
}
