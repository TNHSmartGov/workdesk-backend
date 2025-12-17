package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskCommentDTO;
import com.tnh.baseware.core.entities.task.TaskComment;
import com.tnh.baseware.core.forms.task.TaskCommentEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskCommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Task Comments", description = "API for managing task comments")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-comments")
public class TaskCommentResource extends GenericResource<TaskComment, TaskCommentEditorForm, TaskCommentDTO, UUID> {

    ITaskCommentService taskCommentService;

    public TaskCommentResource(IGenericService<TaskComment, TaskCommentEditorForm, TaskCommentDTO, UUID> service,
                               MessageService messageService,
                               SystemProperties systemProperties,
                               ITaskCommentService taskCommentService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-comments");
        this.taskCommentService = taskCommentService;
    }
}
