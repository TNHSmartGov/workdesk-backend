package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskAttachmentDTO;
import com.tnh.baseware.core.entities.task.TaskAttachment;
import com.tnh.baseware.core.forms.task.TaskAttachmentEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskAttachmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Task Attachments", description = "API for managing task attachments")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-attachments")
public class TaskAttachmentResource extends GenericResource<TaskAttachment, TaskAttachmentEditorForm, TaskAttachmentDTO, UUID> {

    ITaskAttachmentService taskAttachmentService;

    public TaskAttachmentResource(IGenericService<TaskAttachment, TaskAttachmentEditorForm, TaskAttachmentDTO, UUID> service,
                                  MessageService messageService,
                                  SystemProperties systemProperties,
                                  ITaskAttachmentService taskAttachmentService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-attachments");
        this.taskAttachmentService = taskAttachmentService;
    }
}
