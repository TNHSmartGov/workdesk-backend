package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskDependencyDTO;
import com.tnh.baseware.core.entities.task.TaskDependency;
import com.tnh.baseware.core.forms.task.TaskDependencyEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskDependencyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Task Dependencies", description = "API for managing task dependencies")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-dependencies")
public class TaskDependencyResource extends GenericResource<TaskDependency, TaskDependencyEditorForm, TaskDependencyDTO, UUID> {

    ITaskDependencyService taskDependencyService;

    public TaskDependencyResource(IGenericService<TaskDependency, TaskDependencyEditorForm, TaskDependencyDTO, UUID> service,
                                  MessageService messageService,
                                  SystemProperties systemProperties,
                                  ITaskDependencyService taskDependencyService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-dependencies");
        this.taskDependencyService = taskDependencyService;
    }
}
