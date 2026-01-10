package com.tnh.baseware.core.resources.project;

import com.tnh.baseware.core.dtos.project.MemberDTO;
import com.tnh.baseware.core.dtos.project.ProjectMemberDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.forms.project.ProjectMemberEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectMemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Project Members", description = "API for managing project members")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/project-members")
public class ProjectMemberResource
        extends GenericResource<ProjectMember, ProjectMemberEditorForm, ProjectMemberDTO, UUID> {

    IProjectMemberService projectMemberService;

    public ProjectMemberResource(
            IGenericService<ProjectMember, ProjectMemberEditorForm, ProjectMemberDTO, UUID> service,
            MessageService messageService,
            SystemProperties systemProperties,
            IProjectMemberService projectMemberService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/project-members");
        this.projectMemberService = projectMemberService;
    }

    @GetMapping("/by-project/{projectId}")
    @Operation(summary = "Get project members by project id")
    public ResponseEntity<ApiMessageDTO<List<MemberDTO>>> getProjectMembersByProjectId(@PathVariable UUID projectId) {
        var data = projectMemberService.getMembersByProject(projectId);
        ApiMessageDTO<List<MemberDTO>> apiMessageDTO = ApiMessageDTO.<List<MemberDTO>>builder()
                .data(data)
                .result(true)
                .message(messageService.getMessage("project.member.get.success"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(apiMessageDTO);

    }
}
