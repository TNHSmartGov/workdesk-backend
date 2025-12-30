package com.tnh.baseware.core.services.project;

import com.tnh.baseware.core.dtos.project.ProjectDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.enums.project.ProjectAction;
import com.tnh.baseware.core.forms.project.ProjectEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

public interface IProjectService extends IGenericService<Project, ProjectEditorForm, ProjectDTO, UUID> {
    public void performAction(UUID projectId, ProjectAction action);

    public List<Project> getProjectByOrganizationId(UUID organizationId);

    public Page<Project> getProjectByOrganizationId(UUID organizationId, int page, int size);
    Project getOrCreatePersonalProject(UUID userId);
}
