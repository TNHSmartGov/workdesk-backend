package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskRequirementDTO;
import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.forms.task.TaskRequirementEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskRequirementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Task Requirements", description = "API for managing task requirements")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-requirements")
public class TaskRequirementResource extends GenericResource<TaskRequirement, TaskRequirementEditorForm, TaskRequirementDTO, UUID> {

    ITaskRequirementService taskRequirementService;

    public TaskRequirementResource(IGenericService<TaskRequirement, TaskRequirementEditorForm, TaskRequirementDTO, UUID> service,
                                   MessageService messageService,
                                   SystemProperties systemProperties,
                                   ITaskRequirementService taskRequirementService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-requirements");
        this.taskRequirementService = taskRequirementService;
    }
}
