package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskActivityLogDTO;
import com.tnh.baseware.core.entities.task.TaskActivityLog;
import com.tnh.baseware.core.forms.task.TaskActivityLogEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskActivityLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Task Activity Logs", description = "API for managing task activity logs")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-activity-logs")
public class TaskActivityLogResource extends GenericResource<TaskActivityLog, TaskActivityLogEditorForm, TaskActivityLogDTO, UUID> {

    ITaskActivityLogService taskActivityLogService;

    public TaskActivityLogResource(IGenericService<TaskActivityLog, TaskActivityLogEditorForm, TaskActivityLogDTO, UUID> service,
                                   MessageService messageService,
                                   SystemProperties systemProperties,
                                   ITaskActivityLogService taskActivityLogService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-activity-logs");
        this.taskActivityLogService = taskActivityLogService;
    }
}
