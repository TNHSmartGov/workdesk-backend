package com.tnh.baseware.core.services.doc.imp;

import com.tnh.baseware.core.dtos.doc.FileDocumentDTO;
import com.tnh.baseware.core.dtos.doc.FileResource;
import com.tnh.baseware.core.entities.doc.FileDocument;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.doc.FileDocumentEditorForm;
import com.tnh.baseware.core.mappers.doc.IFileDocumentMapper;
import com.tnh.baseware.core.repositories.doc.IFileDocumentRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.doc.IFileDocumentService;
import com.tnh.baseware.core.services.storage.IStorageService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class FileDocumentService extends
        GenericService<FileDocument, FileDocumentEditorForm, FileDocumentDTO, IFileDocumentRepository, IFileDocumentMapper, UUID>
        implements
        IFileDocumentService {

    IStorageService<String> storageService;

    public FileDocumentService(IFileDocumentRepository repository,
            IFileDocumentMapper mapper,
            MessageService messageService,
            IStorageService<String> storageService) {
        super(repository, mapper, messageService, FileDocument.class);
        this.storageService = storageService;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FileDocumentDTO uploadFile(MultipartFile file) {
        String filePath = null;
        try {
            if (file == null || file.isEmpty()) {
                throw new BWCGenericRuntimeException(messageService.getMessage("file.upload.empty"));
            }
            filePath = storageService.uploadFile(file);

            FileDocument doc = FileDocument.builder()
                    .name(file.getOriginalFilename())
                    .size(file.getSize())
                    .url(filePath)
                    .build();

            return mapper.entityToDTO(repository.save(doc));

        } catch (Exception e) {
            if (filePath != null) {
                try {
                    storageService.deleteFile(filePath);
                } catch (Exception ex) {
                    log.warn("Failed to rollback uploaded file: {}", filePath, ex);
                }
            }
            throw new BWCGenericRuntimeException(
                    messageService.getMessage("file.upload.error"), e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<FileDocumentDTO> uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BWCGenericRuntimeException(messageService.getMessage("file.upload.empty"));
        }

        List<FileDocumentDTO> results = new ArrayList<>();
        List<FileDocument> savedDocs = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                FileDocument doc = FileDocument.builder()
                        .name(file.getOriginalFilename())
                        .size(file.getSize())
                        .url(storageService.uploadFile(file))
                        .build();

                repository.save(doc);
                savedDocs.add(doc);
                results.add(mapper.entityToDTO(doc));
            }
            return results;

        } catch (Exception e) {
            log.error("Batch upload failed, rolling back. Error: {}", e.getMessage(), e);

            for (FileDocument doc : savedDocs) {
                try {
                    storageService.deleteFile(doc.getUrl());
                } catch (Exception ex) {
                    log.warn("Rollback failed for file: {}", doc.getName(), ex);
                }
            }

            throw new BWCGenericRuntimeException(
                    messageService.getMessage("file.batch.upload.error"), e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FileResource downloadFile(UUID id) {
        try {
            FileDocument doc = findDocById(id);
            InputStream stream = storageService.downloadFile(doc.getUrl());
            return new FileResource(stream, doc.getName(), null);
        } catch (Exception e) {
            throw new BWCGenericRuntimeException(messageService.getMessage("file.download.error.id", id));
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Map<UUID, FileResource> downloadFiles(List<UUID> ids) {
        Map<UUID, FileResource> results = new HashMap<>();
        List<UUID> failedIds = new ArrayList<>();

        for (UUID id : ids) {
            try {
                results.put(id, downloadFile(id));
            } catch (Exception e) {
                log.error("Failed to download file with ID: {}", id, e);
                failedIds.add(id);
            }
        }

        if (!failedIds.isEmpty()) {
            results.values().forEach(resource -> {
                try {
                    resource.close();
                } catch (IOException e) {
                    log.warn("Failed to close resource", e);
                }
            });
            throw new BWCGenericRuntimeException(messageService.getMessage("file.batch.download.error",
                    failedIds.toString()));
        }

        return results;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteFile(UUID id) {
        FileDocument doc = findDocById(id);
        repository.delete(doc);

        try {
            storageService.deleteFile(doc.getUrl());
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", doc.getUrl(), e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteFiles(List<UUID> ids) {
        List<UUID> failedIds = new ArrayList<>();

        for (UUID id : ids) {
            try {
                deleteFile(id);
            } catch (Exception e) {
                log.error("Failed to delete file with ID: {}", id, e);
                failedIds.add(id);
            }
        }

        if (!failedIds.isEmpty()) {
            throw new BWCGenericRuntimeException(messageService.getMessage("file.batch.delete.error",
                    failedIds.toString()));
        }
    }

    private FileDocument findDocById(UUID id) {
        return repository.findById(id).orElseThrow(() -> {
            log.warn("Entity not found with id: {}", id);
            return new BWCNotFoundException(messageService.getMessage("entity.not.found", id));
        });
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FileDocument upFileDocumentEntity(MultipartFile file) {
        String filePath = storageService.uploadFile(file);

        FileDocument doc = FileDocument.builder()
                .name(file.getOriginalFilename())
                .size(file.getSize())
                .url(filePath)
                .build();

        return repository.save(doc);
    }
}
