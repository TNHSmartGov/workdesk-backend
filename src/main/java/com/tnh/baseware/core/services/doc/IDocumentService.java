package com.tnh.baseware.core.services.doc;

import com.tnh.baseware.core.dtos.doc.DocumentAttachmentDTO;
import com.tnh.baseware.core.dtos.doc.DocumentDTO;
import com.tnh.baseware.core.entities.doc.Document;
import com.tnh.baseware.core.forms.doc.DocumentEditorForm;
import com.tnh.baseware.core.services.IGenericService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IDocumentService extends IGenericService<Document, DocumentEditorForm, DocumentDTO, UUID> {

    DocumentAttachmentDTO uploadAttachment(UUID documentId, MultipartFile file, Boolean isOriginal);

    List<DocumentAttachmentDTO> getAttachments(UUID documentId);
}
