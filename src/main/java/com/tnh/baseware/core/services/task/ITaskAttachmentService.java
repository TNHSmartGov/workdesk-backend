package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskAttachmentDTO;
import com.tnh.baseware.core.entities.task.TaskAttachment;
import com.tnh.baseware.core.forms.task.TaskAttachmentEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ITaskAttachmentService extends IGenericService<TaskAttachment, TaskAttachmentEditorForm, TaskAttachmentDTO, UUID> {
}
