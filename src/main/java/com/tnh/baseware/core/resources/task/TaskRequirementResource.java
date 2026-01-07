package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskRequirementDTO;
import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.forms.task.AssignRequirementForm;
import com.tnh.baseware.core.forms.task.TaskRequirementEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskRequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @Operation(summary = "Add requirement")
    @PostMapping("/{taskId}/requirements")
    public TaskRequirementDTO addRequirement(@PathVariable UUID taskId,
                                             @RequestBody @Valid TaskRequirementEditorForm form) {
        return taskRequirementService.create(taskId, form);
    }

    @Operation(summary = "Update requirement")
    @PutMapping("/{taskId}/requirements/{requirementId}")
    public TaskRequirementDTO updateRequirement(@PathVariable UUID taskId,
                                                @PathVariable UUID requirementId,
                                                @RequestBody @Valid TaskRequirementEditorForm form) {
        return taskRequirementService.update(taskId, requirementId, form);
    }

    @Operation(summary = "Delete requirement")
    @DeleteMapping("/{taskId}/requirements/{requirementId}")
    public void deleteRequirement(@PathVariable UUID taskId, @PathVariable UUID requirementId) {
        taskRequirementService.delete(taskId, requirementId);
    }

    @Operation(summary = "Assign requirement to member")
    @PatchMapping("/{taskId}/requirements/{requirementId}/assign")
    public TaskRequirementDTO assignRequirement(@PathVariable UUID taskId,
                                                @PathVariable UUID requirementId,
                                                @RequestBody @Valid AssignRequirementForm form) {
        return taskRequirementService.assignToMember(taskId, requirementId, form);
    }

    @Operation(summary = "Toggle requirement complete")
    @PatchMapping("/{taskId}/requirements/{requirementId}/toggle")
    public void toggleRequirement(@PathVariable UUID taskId, @PathVariable UUID requirementId) {
        taskRequirementService.toggleComplete(taskId, requirementId);
    }

    @Operation(summary = "Get task requirements")
    @GetMapping("/{taskId}/requirements")
    public List<TaskRequirementDTO> getRequirements(@PathVariable UUID taskId) {
        return taskRequirementService.getByTaskId(taskId);
    }
}
