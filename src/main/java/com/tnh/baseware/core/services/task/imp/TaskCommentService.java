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

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskCommentService extends GenericService<TaskComment, TaskCommentEditorForm, TaskCommentDTO, ITaskCommentRepository, ITaskCommentMapper, UUID> implements ITaskCommentService {

    public TaskCommentService(ITaskCommentRepository repository, ITaskCommentMapper mapper, MessageService messageService) {
        super(repository, mapper, messageService, TaskComment.class);
    }
}
