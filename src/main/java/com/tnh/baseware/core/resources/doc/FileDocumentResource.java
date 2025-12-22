package com.tnh.baseware.core.resources.doc;

import com.tnh.baseware.core.dtos.doc.FileDocumentDTO;
import com.tnh.baseware.core.dtos.doc.FileResource;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.doc.FileDocument;
import com.tnh.baseware.core.forms.doc.FileDocumentEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.doc.IFileDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Tag(name = "File Documents", description = "API for managing file documents")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/files")
public class FileDocumentResource extends GenericResource<FileDocument, FileDocumentEditorForm, FileDocumentDTO, UUID> {

    IFileDocumentService fileDocumentService;

    public FileDocumentResource(IGenericService<FileDocument, FileDocumentEditorForm, FileDocumentDTO, UUID> service,
                                MessageService messageService,
                                SystemProperties systemProperties,
                                IFileDocumentService fileDocumentService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/files");
        this.fileDocumentService = fileDocumentService;
    }

    @Operation(summary = "Upload a file")
    @ApiResponse(responseCode = "200", description = "File uploaded", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiMessageDTO.class)))
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiMessageDTO<FileDocumentDTO>> uploadFile(@RequestParam("file") MultipartFile file,
                                                                     @RequestParam(value = "description", required = false) String description) {
        // description is not used in the interface method uploadFile currently
        var fileDTO = fileDocumentService.uploadFile(file);
        return ResponseEntity.ok(ApiMessageDTO.<FileDocumentDTO>builder()
                .data(fileDTO)
                .result(true)
                .message(messageService.getMessage("file.uploaded"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Download a file by ID")
    @ApiResponse(responseCode = "200", description = "File downloaded", content = @Content(mediaType = "application/octet-stream"))
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable UUID id) throws IOException {
        var fileResource = fileDocumentService.downloadFile(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFileName() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, fileResource.getContentType());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(fileResource.getInputStream()));
    }
}
