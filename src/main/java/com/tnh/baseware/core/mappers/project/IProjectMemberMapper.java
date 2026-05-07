package com.tnh.baseware.core.mappers.project;

import com.tnh.baseware.core.dtos.project.ProjectMemberDTO;
import com.tnh.baseware.core.dtos.project.ProjectRoleDTO;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.entities.project.ProjectRole;
import com.tnh.baseware.core.forms.project.ProjectMemberEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IProjectMemberMapper extends IGenericMapper<ProjectMember, ProjectMemberEditorForm, ProjectMemberDTO> {

    @Override
    @Mapping(source = "projectId", target = "project.id")
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "roleId", target = "projectRole.id")
    ProjectMember formToEntity(ProjectMemberEditorForm form);

    @Override
    @Mapping(source = "projectId", target = "project.id")
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "roleId", target = "projectRole.id")
    void formToEntity(ProjectMemberEditorForm form, @org.mapstruct.MappingTarget ProjectMember entity);

    @Override
    @Mapping(target = "projectRole", expression = "java(mapRoleToDTO(entity.getProjectRole()))")
    ProjectMemberDTO entityToDTO(ProjectMember entity);

    default ProjectRoleDTO mapRoleToDTO(ProjectRole role) {
        if (role == null) {
            return null;
        }
        Set<String> permissions = Set.of();
        if (role.getRolePermissions() != null) {
            permissions = role.getRolePermissions().stream()
                    .map(rp -> rp.getPermission().getValue())
                    .collect(Collectors.toSet());
        }
        return ProjectRoleDTO.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .order(role.getOrder())
                .active(role.isActive())
                .permissions(permissions)
                .build();
    }
}
