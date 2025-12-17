package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskCommentDTO;
import com.tnh.baseware.core.entities.task.TaskComment;
import com.tnh.baseware.core.forms.task.TaskCommentEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ITaskCommentService extends IGenericService<TaskComment, TaskCommentEditorForm, TaskCommentDTO, UUID> {
}
