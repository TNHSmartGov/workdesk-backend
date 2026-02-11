package com.tnh.baseware.core.resources.storage;

import com.tnh.baseware.core.services.storage.IStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@Tag(name = "Storage", description = "API for storage operations")
@RestController
@RequestMapping("${baseware.core.system.api-prefix}/storage")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StorageResource {

    IStorageService<String> storageService;

    @Operation(summary = "View file directly")
    @ApiResponse(responseCode = "200", description = "File content", content = @Content(mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary")))
    @GetMapping("/view")
    public ResponseEntity<Resource> viewFile(@RequestParam("path") String path) {
        InputStream inputStream = storageService.downloadFile(path);

        String fileName = getFileName(path);
        MediaType mediaType = MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(new InputStreamResource(inputStream));
    }

    private String getFileName(String path) {
        if (path == null)
            return "";
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}
