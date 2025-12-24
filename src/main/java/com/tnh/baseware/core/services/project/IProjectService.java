package com.tnh.baseware.core.services.project;

import com.tnh.baseware.core.dtos.project.ProjectDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.enums.project.ProjectAction;
import com.tnh.baseware.core.forms.project.ProjectEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface IProjectService extends IGenericService<Project, ProjectEditorForm, ProjectDTO, UUID> {
    public void performAction(UUID projectId, ProjectAction action);
}
