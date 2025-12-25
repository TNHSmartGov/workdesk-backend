package com.tnh.baseware.core.services.project.imp;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.project.ProjectAttachmentDTO;
import com.tnh.baseware.core.entities.doc.FileDocument;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.ProjectAttachment;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.forms.project.ProjectAttachmentEditorForm;
import com.tnh.baseware.core.mappers.project.IProjectAttachmentMapper;
import com.tnh.baseware.core.repositories.doc.IFileDocumentRepository;
import com.tnh.baseware.core.repositories.project.IProjectAttachmentRepository;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.doc.IFileDocumentService;
import com.tnh.baseware.core.services.project.IProjectAttachmentService;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectAttachmentService extends
        GenericService<ProjectAttachment, ProjectAttachmentEditorForm, ProjectAttachmentDTO, IProjectAttachmentRepository, IProjectAttachmentMapper, UUID>
        implements IProjectAttachmentService {
    private final IProjectRepository projectRepository;
    private final IFileDocumentRepository fileDocumentRepository;
    private final GenericEntityFetcher fetcher;
    IFileDocumentService fileDocumentService;

    public ProjectAttachmentService(IProjectAttachmentRepository repository,
            IProjectAttachmentMapper mapper,
            MessageService messageService,
            IProjectRepository projectRepository,
            IFileDocumentRepository fileDocumentRepository,
            IFileDocumentService fileDocumentService,
            GenericEntityFetcher fetcher) {
        super(repository, mapper, messageService, ProjectAttachment.class);
        this.projectRepository = projectRepository;
        this.fileDocumentRepository = fileDocumentRepository;
        this.fetcher = fetcher;
        this.fileDocumentService = fileDocumentService;
    }

    @Override
    @Transactional
    public ProjectAttachmentDTO create(ProjectAttachmentEditorForm form) {
        var entity = mapper.formToEntity(form, fetcher, fileDocumentRepository, projectRepository);
        entity.setUploader(getCurrentUser());
        return mapper.entityToDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public ProjectAttachmentDTO uploadFile(MultipartFile fileUpload, UUID projectId, String description) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BWCBusinessException(messageService.getMessage("project.not.found")));
        FileDocument doc = fileDocumentService.upFileDocumentEntity(fileUpload);
        var entity = ProjectAttachment.builder()
                .file(doc)
                .project(project)
                .description(description)
                .uploader(currentUser)
                .build();
        return mapper.entityToDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public List<ProjectAttachmentDTO> uploadFiles(List<MultipartFile> filesUpload, UUID projectId, String description) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BWCBusinessException(messageService.getMessage("project.not.found")));
        List<ProjectAttachment> attachments = new ArrayList<>();
        for (MultipartFile f : filesUpload) {
            FileDocument doc = fileDocumentService.upFileDocumentEntity(f);
            attachments.add(ProjectAttachment.builder()
                    .file(doc)
                    .project(project)
                    .description(description)
                    .uploader(currentUser)
                    .build());
        }
        List<ProjectAttachment> saved = repository.saveAll(attachments);
        return saved.stream().map(mapper::entityToDTO).collect(Collectors.toList());
    }
}
