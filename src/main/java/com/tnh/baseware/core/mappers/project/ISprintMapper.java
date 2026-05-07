package com.tnh.baseware.core.mappers.project;

import com.tnh.baseware.core.dtos.project.SprintDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.Sprint;
import com.tnh.baseware.core.forms.project.SprintEditorForm;
import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ISprintMapper extends IGenericMapper<Sprint, SprintEditorForm, SprintDTO> {

    @Mapping(target = "project", expression = "java(fetcher.formToEntity(projectRepo, form.getProjectId()))")
    @Mapping(target = "status", defaultValue = "PENDING")
    Sprint formToEntity(SprintEditorForm form, @Context GenericEntityFetcher fetcher,
            @Context IProjectRepository projectRepo);

    @Mapping(target = "project", expression = "java(fetcher.formToEntity(projectRepo, form.getProjectId()))")
    void updateEntityFromForm(SprintEditorForm form, @MappingTarget Sprint entity,
            @Context GenericEntityFetcher fetcher, @Context IProjectRepository projectRepo);

    @Mapping(source = "project.id", target = "projectId")
    SprintDTO entityToDTO(Sprint entity);
}
