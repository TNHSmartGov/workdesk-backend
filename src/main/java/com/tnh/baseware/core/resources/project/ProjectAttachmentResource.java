package com.tnh.baseware.core.resources.project;

import com.tnh.baseware.core.dtos.project.ProjectAttachmentDTO;
import com.tnh.baseware.core.entities.project.ProjectAttachment;
import com.tnh.baseware.core.forms.project.ProjectAttachmentEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectAttachmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Project's Attachments", description = "API for managing project's attachment")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/project-attachments")
public class ProjectAttachmentResource extends GenericResource<ProjectAttachment, ProjectAttachmentEditorForm, ProjectAttachmentDTO, UUID> {
    private final IProjectAttachmentService projectAttachmentService;

    public ProjectAttachmentResource(IGenericService<ProjectAttachment, ProjectAttachmentEditorForm, ProjectAttachmentDTO, UUID> service,
                                     MessageService messageService,
                                     SystemProperties systemProperties,
                                     IProjectAttachmentService projectAttachmentService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/project-attachments");
        this.projectAttachmentService = projectAttachmentService;
    }
}
