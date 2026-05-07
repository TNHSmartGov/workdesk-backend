package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskCommentAttachmentDTO;
import com.tnh.baseware.core.entities.task.TaskCommentAttachment;
import com.tnh.baseware.core.forms.task.TaskCommentAttachmentEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface ITaskCommentAttachmentService extends
        IGenericService<TaskCommentAttachment, TaskCommentAttachmentEditorForm, TaskCommentAttachmentDTO, UUID> {
    TaskCommentAttachmentDTO uploadAttachment(UUID commentId, MultipartFile file);

    List<TaskCommentAttachmentDTO> uploadAttachments(UUID commentId, List<MultipartFile> files);

    List<TaskCommentAttachmentDTO> findByCommentId(UUID commentId);
}
