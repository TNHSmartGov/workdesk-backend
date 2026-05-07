package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskDocumentDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.task.TaskDocument;
import com.tnh.baseware.core.forms.task.TaskDocumentEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Task Documents", description = "API for managing task-document relationships")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-documents")
public class TaskDocumentResource extends GenericResource<TaskDocument, TaskDocumentEditorForm, TaskDocumentDTO, UUID> {

    ITaskDocumentService taskDocumentService;

    public TaskDocumentResource(IGenericService<TaskDocument, TaskDocumentEditorForm, TaskDocumentDTO, UUID> service,
            MessageService messageService,
            SystemProperties systemProperties,
            ITaskDocumentService taskDocumentService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-documents");
        this.taskDocumentService = taskDocumentService;
    }

    @Operation(summary = "Get all documents linked to a task", description = "Retrieve all documents associated with a specific task")
    @GetMapping("/by-task/{taskId}")
    public ResponseEntity<ApiMessageDTO<List<TaskDocumentDTO>>> getDocumentsByTask(@PathVariable UUID taskId) {
        var documents = taskDocumentService.getDocumentsByTask(taskId);

        return ResponseEntity.ok(ApiMessageDTO.<List<TaskDocumentDTO>>builder()
                .data(documents)
                .result(true)
                .message(messageService.getMessage("task.documents.retrieved.success"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Get all tasks linked to a document", description = "Retrieve all tasks associated with a specific document")
    @GetMapping("/by-document/{documentId}")
    public ResponseEntity<ApiMessageDTO<List<TaskDocumentDTO>>> getTasksByDocument(@PathVariable UUID documentId) {
        var tasks = taskDocumentService.getTasksByDocument(documentId);

        return ResponseEntity.ok(ApiMessageDTO.<List<TaskDocumentDTO>>builder()
                .data(tasks)
                .result(true)
                .message(messageService.getMessage("document.tasks.retrieved.success"))
                .code(HttpStatus.OK.value())
                .build());
    }
}
