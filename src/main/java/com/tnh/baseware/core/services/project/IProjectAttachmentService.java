package com.tnh.baseware.core.services.project;

import com.tnh.baseware.core.dtos.project.ProjectAttachmentDTO;
import com.tnh.baseware.core.entities.project.ProjectAttachment;
import com.tnh.baseware.core.forms.project.ProjectAttachmentEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface IProjectAttachmentService extends IGenericService<ProjectAttachment, ProjectAttachmentEditorForm, ProjectAttachmentDTO, UUID> {

}
