package com.tnh.baseware.core.mappers.project;

import com.tnh.baseware.core.dtos.project.ProjectDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.project.ProjectMemberRole;
import com.tnh.baseware.core.forms.project.ProjectEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.repositories.project.IProjectMemberRepository;

import java.util.Optional;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IProjectMapper extends IGenericMapper<Project, ProjectEditorForm, ProjectDTO> {

    @Override
    Project formToEntity(ProjectEditorForm form);

    @Override
    void formToEntity(ProjectEditorForm form, @MappingTarget Project entity);

    @Mapping(target = "memberRole", expression = "java(getMemberRole(entity, currentUser, projectMemberRepository))")
    ProjectDTO entityToDTO(Project entity, @Context User currentUser,
            @Context IProjectMemberRepository projectMemberRepository);

    @Override
    default ProjectDTO entityToDTO(Project entity) {
        return entityToDTO(entity, null, null);
    }

    default ProjectMemberRole getMemberRole(Project project, @Context User currentUser,
            @Context IProjectMemberRepository projectMemberRepository) {
        if (currentUser == null || project == null) {
            return null;
        }
        Optional<ProjectMember> member = projectMemberRepository.findByProjectIdAndUserId(project.getId(),
                currentUser.getId());
        return member.map(ProjectMember::getRole).orElse(null);
    }
}
