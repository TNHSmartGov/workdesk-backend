package com.tnh.baseware.core.resources.project;

import com.tnh.baseware.core.dtos.project.ProjectDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.forms.project.ProjectEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Projects", description = "API for managing projects")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/projects")
public class ProjectResource extends GenericResource<Project, ProjectEditorForm, ProjectDTO, UUID> {

    IProjectService projectService;

    public ProjectResource(IGenericService<Project, ProjectEditorForm, ProjectDTO, UUID> service,
                           MessageService messageService,
                           SystemProperties systemProperties,
                           IProjectService projectService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/projects");
        this.projectService = projectService;
    }
}
