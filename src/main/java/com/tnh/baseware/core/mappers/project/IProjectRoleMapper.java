package com.tnh.baseware.core.mappers.project;

import com.tnh.baseware.core.dtos.project.ProjectRoleDTO;
import com.tnh.baseware.core.entities.project.ProjectRole;
import com.tnh.baseware.core.forms.project.ProjectRoleEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IProjectRoleMapper extends IGenericMapper<ProjectRole, ProjectRoleEditorForm, ProjectRoleDTO> {

    @Override
    @Mapping(target = "permissions", expression = "java(mapPermissions(entity))")
    ProjectRoleDTO entityToDTO(ProjectRole entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "active", constant = "true")
    ProjectRole formToEntity(ProjectRoleEditorForm form);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    void formToEntity(ProjectRoleEditorForm form, @MappingTarget ProjectRole entity);

    default Set<String> mapPermissions(ProjectRole entity) {
        if (entity.getRolePermissions() == null) {
            return Set.of();
        }
        return entity.getRolePermissions().stream()
                .map(rp -> rp.getPermission().getValue())
                .collect(Collectors.toSet());
    }
}
