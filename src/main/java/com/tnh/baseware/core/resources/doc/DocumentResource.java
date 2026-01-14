package com.tnh.baseware.core.resources.doc;

import com.tnh.baseware.core.dtos.doc.DocumentAttachmentDTO;
import com.tnh.baseware.core.dtos.doc.DocumentDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.doc.Document;
import com.tnh.baseware.core.forms.doc.DocumentEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.doc.IDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Documents", description = "API for managing documents (văn bản)")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/documents")
public class DocumentResource extends GenericResource<Document, DocumentEditorForm, DocumentDTO, UUID> {

    IDocumentService documentService;

    public DocumentResource(IGenericService<Document, DocumentEditorForm, DocumentDTO, UUID> service,
            MessageService messageService,
            SystemProperties systemProperties,
            IDocumentService documentService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/documents");
        this.documentService = documentService;
    }

    @Operation(summary = "Upload attachment to document", description = "Upload a single file as document attachment")
    @PostMapping(value = "/attachment/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiMessageDTO<DocumentAttachmentDTO>> uploadAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "is_original", defaultValue = "false") Boolean isOriginal) {

        var attachment = documentService.uploadAttachment(id, file, isOriginal);

        return ResponseEntity.ok(ApiMessageDTO.<DocumentAttachmentDTO>builder()
                .data(attachment)
                .result(true)
                .message(messageService.getMessage("file.upload.success"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Get all attachments of document", description = "Retrieve all file attachments for a specific document")
    @GetMapping(value = "/get-attachment/{id}")
    public ResponseEntity<ApiMessageDTO<List<DocumentAttachmentDTO>>> getAttachments(@PathVariable UUID id) {
        var attachments = documentService.getAttachments(id);

        return ResponseEntity.ok(ApiMessageDTO.<List<DocumentAttachmentDTO>>builder()
                .data(attachments)
                .result(true)
                .message(messageService.getMessage("file.get.attached.success"))
                .code(HttpStatus.OK.value())
                .build());
    }
}
