package com.tnh.baseware.core.services.project.imp;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.project.ProjectAttachmentDTO;
import com.tnh.baseware.core.entities.project.ProjectAttachment;
import com.tnh.baseware.core.forms.project.ProjectAttachmentEditorForm;
import com.tnh.baseware.core.mappers.project.IProjectAttachmentMapper;
import com.tnh.baseware.core.repositories.doc.IFileDocumentRepository;
import com.tnh.baseware.core.repositories.project.IProjectAttachmentRepository;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectAttachmentService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectAttachmentService extends GenericService<ProjectAttachment, ProjectAttachmentEditorForm, ProjectAttachmentDTO, IProjectAttachmentRepository, IProjectAttachmentMapper, UUID> implements IProjectAttachmentService {
    private final IProjectRepository  projectRepository;
    private final IFileDocumentRepository fileDocumentRepository;
    private final GenericEntityFetcher fetcher;

    public ProjectAttachmentService(IProjectAttachmentRepository repository,
                                    IProjectAttachmentMapper mapper,
                                    MessageService messageService,
                                    IProjectRepository  projectRepository,
                                    IFileDocumentRepository fileDocumentRepository,
                                    GenericEntityFetcher fetcher) {
        super(repository, mapper, messageService, ProjectAttachment.class);
        this.projectRepository = projectRepository;
        this.fileDocumentRepository = fileDocumentRepository;
        this.fetcher = fetcher;
    }

    @Override
    @Transactional
    public ProjectAttachmentDTO create(ProjectAttachmentEditorForm form) {
        var entity = mapper.formToEntity(form, fetcher, fileDocumentRepository, projectRepository);
        entity.setUploader(getCurrentUser());
        return mapper.entityToDTO(repository.save(entity));
    }
}
