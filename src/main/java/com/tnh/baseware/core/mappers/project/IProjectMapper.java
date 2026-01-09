package com.tnh.baseware.core.mappers.project;

import com.tnh.baseware.core.dtos.project.ProjectDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.forms.project.ProjectEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IProjectMapper extends IGenericMapper<Project, ProjectEditorForm, ProjectDTO> {

    @Override
    Project formToEntity(ProjectEditorForm form);

    @Override
    void formToEntity(ProjectEditorForm form, @org.mapstruct.MappingTarget Project entity);

    ProjectDTO entityToDTO(Project entity);
}
