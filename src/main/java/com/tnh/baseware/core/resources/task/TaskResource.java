package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.forms.task.TaskActionForm;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.forms.task.UpdateProgressForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskCommandService;
import com.tnh.baseware.core.services.task.ITaskQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Tasks", description = "API for managing tasks")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/tasks")
public class TaskResource extends GenericResource<Task, TaskEditorForm, TaskDTO, UUID> {

    ITaskCommandService taskCommandService;
    ITaskQueryService taskQueryService;

    public TaskResource(MessageService messageService,
                        SystemProperties systemProperties,
                        ITaskCommandService taskCommandService,
                        ITaskQueryService taskQueryService) {
        super(taskCommandService, messageService, systemProperties.getApiPrefix() + "/tasks");
        this.taskCommandService = taskCommandService;
        this.taskQueryService = taskQueryService;
    }

    @Operation(summary = "Perform an action on task")
    @PostMapping(value = "/{id}/actions")
    public void performAction(@PathVariable UUID id,
                              @RequestBody @Valid TaskActionForm form) {
        taskCommandService.performAction(id, form.getAction());
    }

    @Operation(summary = "Update personal progress")
    @PatchMapping("/{id}/progress")
    public void updateProgress(@PathVariable UUID id,
                               @RequestBody @Valid UpdateProgressForm form) {
        taskCommandService.updatePersonalProgress(id, form.getProgress());
    }
}
