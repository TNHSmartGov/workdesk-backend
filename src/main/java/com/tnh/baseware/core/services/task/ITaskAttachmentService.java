package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskAttachmentDTO;
import com.tnh.baseware.core.entities.task.TaskAttachment;
import com.tnh.baseware.core.forms.task.TaskAttachmentEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface ITaskAttachmentService
        extends IGenericService<TaskAttachment, TaskAttachmentEditorForm, TaskAttachmentDTO, UUID> {
    TaskAttachmentDTO uploadFile(MultipartFile fileUpload, UUID taskId, String description);

    List<TaskAttachmentDTO> uploadFiles(List<MultipartFile> filesUpload, UUID taskId, String description);
}
