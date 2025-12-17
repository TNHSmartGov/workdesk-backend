package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskCommentAttachmentDTO;
import com.tnh.baseware.core.entities.task.TaskCommentAttachment;
import com.tnh.baseware.core.forms.task.TaskCommentAttachmentEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ITaskCommentAttachmentService extends IGenericService<TaskCommentAttachment, TaskCommentAttachmentEditorForm, TaskCommentAttachmentDTO, UUID> {
}
