package com.tnh.baseware.core.services.doc;

import com.tnh.baseware.core.dtos.doc.FileDocumentDTO;
import com.tnh.baseware.core.dtos.doc.FileResource;
import com.tnh.baseware.core.entities.doc.FileDocument;
import com.tnh.baseware.core.forms.doc.FileDocumentEditorForm;
import com.tnh.baseware.core.services.IGenericService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IFileDocumentService extends
        IGenericService<FileDocument, FileDocumentEditorForm, FileDocumentDTO, UUID> {
    FileDocumentDTO uploadFile(MultipartFile file);

    List<FileDocumentDTO> uploadFiles(List<MultipartFile> files);

    FileResource downloadFile(UUID id);

    Map<UUID, FileResource> downloadFiles(List<UUID> ids);

    void deleteFile(UUID id);

    void deleteFiles(List<UUID> ids);

    FileDocument upFileDocumentEntity(MultipartFile file);
}
