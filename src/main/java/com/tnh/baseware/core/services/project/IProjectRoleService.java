package com.tnh.baseware.core.services.project;

import com.tnh.baseware.core.dtos.project.ProjectRoleDTO;
import com.tnh.baseware.core.entities.project.ProjectRole;
import com.tnh.baseware.core.forms.project.ProjectRoleEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.List;
import java.util.UUID;

public interface IProjectRoleService extends IGenericService<ProjectRole, ProjectRoleEditorForm, ProjectRoleDTO, UUID> {

    List<ProjectRoleDTO> getAllRoles();

    ProjectRoleDTO getRoleById(UUID id);
}
