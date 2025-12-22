package com.tnh.baseware.core.services.project;

import com.tnh.baseware.core.dtos.project.ProjectMemberDTO;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.forms.project.ProjectMemberEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface IProjectMemberService extends IGenericService<ProjectMember, ProjectMemberEditorForm, ProjectMemberDTO, UUID> {
}
