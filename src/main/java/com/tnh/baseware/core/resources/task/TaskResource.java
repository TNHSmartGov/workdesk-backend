package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.dtos.task.TaskMemberDTO;
import com.tnh.baseware.core.dtos.task.TaskRequirementDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.forms.task.*;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskCommandService;
import com.tnh.baseware.core.services.task.ITaskMemberService;
import com.tnh.baseware.core.services.task.ITaskQueryService;
import com.tnh.baseware.core.services.task.ITaskRequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Tasks", description = "API for managing tasks")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/tasks")
public class TaskResource extends GenericResource<Task, TaskEditorForm, TaskDTO, UUID> {
    ITaskMemberService taskMemberService;
    ITaskRequirementService taskRequirementService;
    ITaskCommandService taskCommandService;
    ITaskQueryService taskQueryService;

    public TaskResource(MessageService messageService,
            SystemProperties systemProperties,
            ITaskCommandService taskCommandService,
            ITaskQueryService taskQueryService,
            ITaskMemberService taskMemberService,
            ITaskRequirementService taskRequirementService) {
        super(taskCommandService, messageService, systemProperties.getApiPrefix() + "/tasks");
        this.taskCommandService = taskCommandService;
        this.taskQueryService = taskQueryService;
        this.taskMemberService = taskMemberService;
        this.taskRequirementService = taskRequirementService;
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

    @Operation(summary = "Assign members to task")
    @PostMapping("/{id}/members")
    public List<TaskMemberDTO> assignMembers(@PathVariable UUID id,
            @RequestBody @Valid List<TaskMemberEditorForm> forms) {
        return taskMemberService.assignMembers(id, forms);
    }

    @Operation(summary = "Update task member")
    @PutMapping("/{id}/members/{memberId}")
    public TaskMemberDTO updateMember(@PathVariable UUID id,
            @PathVariable UUID memberId,
            @RequestBody @Valid TaskMemberEditorForm form) {
        return taskMemberService.updateMember(id, memberId, form);
    }

    @Operation(summary = "Remove member from task")
    @DeleteMapping("/{id}/members/{memberId}")
    public void removeMember(@PathVariable UUID id, @PathVariable UUID memberId) {
        taskMemberService.removeMember(id, memberId);
    }

    @Operation(summary = "Get task members")
    @GetMapping("/{id}/members")
    public List<TaskMemberDTO> getMembers(@PathVariable UUID id) {
        return taskMemberService.getTaskMembers(id);
    }

    @Operation(summary = "Add requirement to task")
    @PostMapping("/{id}/requirements")
    public TaskRequirementDTO addRequirement(@PathVariable UUID id,
            @RequestBody @Valid TaskRequirementEditorForm form) {
        return taskRequirementService.create(id, form);
    }

    @Operation(summary = "Update task requirement")
    @PutMapping("/{id}/requirements/{requirementId}")
    public TaskRequirementDTO updateRequirement(@PathVariable UUID id,
            @PathVariable UUID requirementId,
            @RequestBody @Valid TaskRequirementEditorForm form) {
        return taskRequirementService.update(id, requirementId, form);
    }

    @Operation(summary = "Delete task requirement")
    @DeleteMapping("/{id}/requirements/{requirementId}")
    public ResponseEntity<ApiMessageDTO<Integer>> deleteRequirement(@PathVariable UUID id,
            @PathVariable UUID requirementId) {
        taskRequirementService.delete(id, requirementId);
        return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                .data(1)
                .result(true)
                .message(messageService.getMessage("requirement.deleted"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Assign task requirement to member")
    @PatchMapping("/{id}/requirements/{requirementId}/assign")
    public TaskRequirementDTO assignRequirement(@PathVariable UUID id,
            @PathVariable UUID requirementId,
            @RequestBody @Valid AssignRequirementForm form) {
        return taskRequirementService.assignToMember(id, requirementId, form);
    }

    @Operation(summary = "Toggle task requirement complete status")
    @PatchMapping("/{id}/requirements/{requirementId}/toggle")
    public void toggleRequirement(@PathVariable UUID id, @PathVariable UUID requirementId) {
        taskRequirementService.toggleComplete(id, requirementId);
    }

    @Operation(summary = "Get task requirements")
    @GetMapping("/{id}/requirements")
    public List<TaskRequirementDTO> getRequirements(@PathVariable UUID id) {
        return taskRequirementService.getByTaskId(id);
    }
}
