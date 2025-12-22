package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskListDTO;
import com.tnh.baseware.core.entities.task.TaskList;
import com.tnh.baseware.core.forms.task.TaskListEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskListService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Task Lists", description = "API for managing task lists")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-lists")
public class TaskListResource extends GenericResource<TaskList, TaskListEditorForm, TaskListDTO, UUID> {

    ITaskListService taskListService;

    public TaskListResource(IGenericService<TaskList, TaskListEditorForm, TaskListDTO, UUID> service,
                            MessageService messageService,
                            SystemProperties systemProperties,
                            ITaskListService taskListService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-lists");
        this.taskListService = taskListService;
    }
}
