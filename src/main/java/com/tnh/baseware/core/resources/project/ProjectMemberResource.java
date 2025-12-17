package com.tnh.baseware.core.resources.project;

import com.tnh.baseware.core.dtos.project.ProjectMemberDTO;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.forms.project.ProjectMemberEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectMemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Project Members", description = "API for managing project members")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/project-members")
public class ProjectMemberResource extends GenericResource<ProjectMember, ProjectMemberEditorForm, ProjectMemberDTO, UUID> {

    IProjectMemberService projectMemberService;

    public ProjectMemberResource(IGenericService<ProjectMember, ProjectMemberEditorForm, ProjectMemberDTO, UUID> service,
                                 MessageService messageService,
                                 SystemProperties systemProperties,
                                 IProjectMemberService projectMemberService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/project-members");
        this.projectMemberService = projectMemberService;
    }
}
