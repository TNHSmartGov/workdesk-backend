package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskCommentAttachmentDTO;
import com.tnh.baseware.core.dtos.task.TaskCommentDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.task.TaskComment;
import com.tnh.baseware.core.forms.task.TaskCommentEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskCommentAttachmentService;
import com.tnh.baseware.core.services.task.ITaskCommentService;

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

@Tag(name = "Task Comments", description = "API for managing task comments")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-comments")
public class TaskCommentResource extends GenericResource<TaskComment, TaskCommentEditorForm, TaskCommentDTO, UUID> {

    ITaskCommentService taskCommentService;
    ITaskCommentAttachmentService taskCommentAttachmentService;

    public TaskCommentResource(IGenericService<TaskComment, TaskCommentEditorForm, TaskCommentDTO, UUID> service,
            MessageService messageService,
            SystemProperties systemProperties,
            ITaskCommentService taskCommentService,
            ITaskCommentAttachmentService taskCommentAttachmentService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-comments");
        this.taskCommentService = taskCommentService;
        this.taskCommentAttachmentService = taskCommentAttachmentService;
    }

    @Operation(summary = "Upload task comment attachment")
    @PostMapping("/attachments/{commentId}")
    public ResponseEntity<ApiMessageDTO<List<TaskCommentAttachmentDTO>>> uploadAttachments(@PathVariable UUID commentId,
            @RequestParam("files") List<MultipartFile> files) {
        var attachments = taskCommentAttachmentService.uploadAttachments(commentId, files);
        return ResponseEntity.ok(ApiMessageDTO.<List<TaskCommentAttachmentDTO>>builder()
                .data(attachments)
                .result(true)
                .message(messageService.getMessage("task.comment.attachment.uploaded.success"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Get task comment attachments")
    @GetMapping("/attachments/{commentId}")
    public ResponseEntity<ApiMessageDTO<List<TaskCommentAttachmentDTO>>> getAttachments(@PathVariable UUID commentId) {

        var attachments = taskCommentAttachmentService.findByCommentId(commentId);
        return ResponseEntity.ok(ApiMessageDTO.<List<TaskCommentAttachmentDTO>>builder()
                .data(attachments)
                .result(true)
                .message(messageService.getMessage("task.comment.attachment.found.success"))
                .code(HttpStatus.OK.value())
                .build());
    }
}
