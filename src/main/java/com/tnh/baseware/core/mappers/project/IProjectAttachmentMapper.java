package com.tnh.baseware.core.mappers.project;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.project.ProjectAttachmentDTO;
import com.tnh.baseware.core.dtos.project.ProjectDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.ProjectAttachment;
import com.tnh.baseware.core.forms.project.ProjectAttachmentEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.repositories.doc.IFileDocumentRepository;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IProjectAttachmentMapper extends IGenericMapper<ProjectAttachment, ProjectAttachmentEditorForm, ProjectAttachmentDTO> {
    @Mapping(target = "project", expression = "java(fetcher.formToEntity(projectRepository, form.getProjectId()))")
    @Mapping(target = "file", expression = "java(fetcher.formToEntity(fileDocumentRepository, form.getFileDocumentId()))")
    ProjectAttachment formToEntity(ProjectAttachmentEditorForm form,
                                   @Context GenericEntityFetcher fetcher,
                                   @Context IFileDocumentRepository fileDocumentRepository,
                                   @Context IProjectRepository projectRepository);
}
