package com.tnh.baseware.core.services.doc.imp;

import com.tnh.baseware.core.dtos.doc.DocumentAttachmentDTO;
import com.tnh.baseware.core.dtos.doc.DocumentDTO;
import com.tnh.baseware.core.entities.doc.Document;
import com.tnh.baseware.core.entities.doc.DocumentAttachment;
import com.tnh.baseware.core.entities.doc.FileDocument;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.doc.DocumentEditorForm;
import com.tnh.baseware.core.mappers.doc.IDocumentAttachmentMapper;
import com.tnh.baseware.core.mappers.doc.IDocumentMapper;
import com.tnh.baseware.core.repositories.doc.IDocumentAttachmentRepository;
import com.tnh.baseware.core.repositories.doc.IDocumentRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.doc.IDocumentService;
import com.tnh.baseware.core.services.doc.IFileDocumentService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DocumentService extends
        GenericService<Document, DocumentEditorForm, DocumentDTO, IDocumentRepository, IDocumentMapper, UUID>
        implements IDocumentService {

    IFileDocumentService fileDocumentService;
    IDocumentAttachmentRepository documentAttachmentRepository;
    IDocumentAttachmentMapper documentAttachmentMapper;

    public DocumentService(IDocumentRepository repository,
            IDocumentMapper mapper,
            MessageService messageService,
            IFileDocumentService fileDocumentService,
            IDocumentAttachmentRepository documentAttachmentRepository,
            IDocumentAttachmentMapper documentAttachmentMapper) {
        super(repository, mapper, messageService, Document.class);
        this.fileDocumentService = fileDocumentService;
        this.documentAttachmentRepository = documentAttachmentRepository;
        this.documentAttachmentMapper = documentAttachmentMapper;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DocumentDTO create(DocumentEditorForm form) {
        var entity = mapper.formToEntity(form);
        var saved = repository.save(entity);
        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DocumentDTO update(UUID id, DocumentEditorForm form) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("document.not.found", id)));
        mapper.formToEntity(form, entity);
        return mapper.entityToDTO(repository.save(entity));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DocumentAttachmentDTO uploadAttachment(UUID documentId, MultipartFile file, Boolean isOriginal) {
        var document = repository.findById(documentId).orElseThrow(
                () -> new BWCNotFoundException(messageService.getMessage("document.not.found", documentId)));

        FileDocument fileDoc = fileDocumentService.upFileDocumentEntity(file);
        var attachment = DocumentAttachment.builder()
                .document(document)
                .file(fileDoc)
                .original(isOriginal != null ? isOriginal : false)
                .size(file.getSize() / 1024.0) // Convert to KB
                .build();

        var saved = documentAttachmentRepository.save(attachment);
        return documentAttachmentMapper.entityToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentAttachmentDTO> getAttachments(UUID documentId) {
        // Verify document exists
        repository.findById(documentId).orElseThrow(
                () -> new BWCNotFoundException(messageService.getMessage("document.not.found", documentId)));

        return documentAttachmentRepository.findAllByDocument_Id(documentId).stream()
                .map(documentAttachmentMapper::entityToDTO)
                .toList();
    }
}
