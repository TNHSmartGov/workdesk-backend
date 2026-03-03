package com.tnh.baseware.core.resources.project;

import com.tnh.baseware.core.dtos.project.ProjectRoleDTO;
import com.tnh.baseware.core.entities.project.ProjectRole;
import com.tnh.baseware.core.enums.project.ProjectPermission;
import com.tnh.baseware.core.forms.project.ProjectRoleEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectRoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Project Roles", description = "API for managing project roles")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/project-roles")
public class ProjectRoleResource extends GenericResource<ProjectRole, ProjectRoleEditorForm, ProjectRoleDTO, UUID> {

    public ProjectRoleResource(IGenericService<ProjectRole, ProjectRoleEditorForm, ProjectRoleDTO, UUID> service,
            MessageService messageService,
            SystemProperties systemProperties,
            IProjectRoleService projectRoleService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/project-roles");
    }

    @Operation(summary = "Get all project permissions")

    @GetMapping("/permissions")
    public ResponseEntity<List<Map<String, String>>> getAllPermissions() {
        List<Map<String, String>> permissions = Arrays.stream(ProjectPermission.values())
                .map(p -> Map.of(
                        "value", p.getValue(),
                        "name", p.getName(),
                        "displayName", p.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(permissions);
    }
}
