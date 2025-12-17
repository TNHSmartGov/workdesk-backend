package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskCommentAttachmentDTO;
import com.tnh.baseware.core.entities.task.TaskCommentAttachment;
import com.tnh.baseware.core.forms.task.TaskCommentAttachmentEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskCommentAttachmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Task Comment Attachments", description = "API for managing task comment attachments")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-comment-attachments")
public class TaskCommentAttachmentResource extends GenericResource<TaskCommentAttachment, TaskCommentAttachmentEditorForm, TaskCommentAttachmentDTO, UUID> {

    ITaskCommentAttachmentService taskCommentAttachmentService;

    public TaskCommentAttachmentResource(IGenericService<TaskCommentAttachment, TaskCommentAttachmentEditorForm, TaskCommentAttachmentDTO, UUID> service,
                                         MessageService messageService,
                                         SystemProperties systemProperties,
                                         ITaskCommentAttachmentService taskCommentAttachmentService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-comment-attachments");
        this.taskCommentAttachmentService = taskCommentAttachmentService;
    }
}
