package com.tnh.baseware.core.mappers.project;

import com.tnh.baseware.core.dtos.project.ProjectMemberDTO;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.forms.project.ProjectMemberEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IProjectMemberMapper extends IGenericMapper<ProjectMember, ProjectMemberEditorForm, ProjectMemberDTO> {

    @Override
    @Mapping(source = "projectId", target = "project.id")
    @Mapping(source = "userId", target = "user.id")
    ProjectMember formToEntity(ProjectMemberEditorForm form);

    @Override
    @Mapping(source = "projectId", target = "project.id")
    @Mapping(source = "userId", target = "user.id")
    void formToEntity(ProjectMemberEditorForm form, @org.mapstruct.MappingTarget ProjectMember entity);
}
