package com.tnh.baseware.core.resources.project;

import com.tnh.baseware.core.dtos.project.ProjectAttachmentDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.project.ProjectAttachment;
import com.tnh.baseware.core.forms.project.ProjectAttachmentEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectAttachmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Project's Attachments", description = "API for managing project's attachment")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/project-attachments")
public class ProjectAttachmentResource
        extends GenericResource<ProjectAttachment, ProjectAttachmentEditorForm, ProjectAttachmentDTO, UUID> {
    private final IProjectAttachmentService projectAttachmentService;

    public ProjectAttachmentResource(
            IGenericService<ProjectAttachment, ProjectAttachmentEditorForm, ProjectAttachmentDTO, UUID> service,
            MessageService messageService,
            SystemProperties systemProperties,
            IProjectAttachmentService projectAttachmentService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/project-attachments");
        this.projectAttachmentService = projectAttachmentService;
    }

    @Operation(summary = "Attach file to project")
    @PostMapping(value = "/{projectId}")
    public ResponseEntity<ApiMessageDTO<Integer>> uploadFile(@PathVariable UUID projectId,
            @RequestParam("file") MultipartFile attachedFile, @RequestParam("description") String description) {
        projectAttachmentService.uploadFile(attachedFile, projectId, description);
        ApiMessageDTO<Integer> apiMessageDTO = ApiMessageDTO.<Integer>builder()
                .data(1)
                .result(true)
                .message(messageService.getMessage("file.add.attached.success"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(apiMessageDTO);
    }

    @Operation(summary = "Get all files of project ")
    @GetMapping(value = "/{projectId}")
    public ResponseEntity<ApiMessageDTO<List<ProjectAttachmentDTO>>> getAttackmentByProject(
            @PathVariable UUID projectId) {
        var attachments = projectAttachmentService.getAttackmentByProject(projectId);
        ApiMessageDTO<List<ProjectAttachmentDTO>> apiMessageDTO = ApiMessageDTO.<List<ProjectAttachmentDTO>>builder()
                .data(attachments)
                .result(true)
                .message(messageService.getMessage("file.get.attached.success"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(apiMessageDTO);
    }
}
