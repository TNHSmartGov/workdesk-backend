package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskCategoryDTO;
import com.tnh.baseware.core.entities.task.TaskCategory;
import com.tnh.baseware.core.forms.task.TaskCategoryEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Task Categories", description = "API for managing task categories with tree structure")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-categories")
public class TaskCategoryResource extends GenericResource<TaskCategory, TaskCategoryEditorForm, TaskCategoryDTO, UUID> {

    public TaskCategoryResource(IGenericService<TaskCategory, TaskCategoryEditorForm, TaskCategoryDTO, UUID> service,
            MessageService messageService,
            SystemProperties systemProperties) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-categories");
    }
}
