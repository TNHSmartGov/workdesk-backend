package com.tnh.baseware.core.services.project;

import com.tnh.baseware.core.dtos.project.ProjectAttachmentDTO;
import com.tnh.baseware.core.entities.project.ProjectAttachment;
import com.tnh.baseware.core.forms.project.ProjectAttachmentEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface IProjectAttachmentService
        extends IGenericService<ProjectAttachment, ProjectAttachmentEditorForm, ProjectAttachmentDTO, UUID> {

    ProjectAttachmentDTO uploadFile(MultipartFile fileUpload, UUID projectId, String description);

    List<ProjectAttachmentDTO> uploadFiles(List<MultipartFile> filesUpload, UUID projectId, String description);

    List<ProjectAttachmentDTO> getAttackmentByProject(UUID projectId);
}
