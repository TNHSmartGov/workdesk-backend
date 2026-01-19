package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.dtos.task.TaskMemberDTO;
import com.tnh.baseware.core.dtos.task.TaskRequirementDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.enums.ApiResponseType;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.forms.task.*;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskCommandService;
import com.tnh.baseware.core.services.task.ITaskMemberService;
import com.tnh.baseware.core.services.task.ITaskQueryService;
import com.tnh.baseware.core.services.task.ITaskRequirementService;
import com.tnh.baseware.core.specs.SearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
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

    public TaskResource(
            MessageService messageService,
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
            @RequestBody List<TaskMemberEditorForm> forms) {
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
    public ResponseEntity<ApiMessageDTO<List<TaskMemberDTO>>> getMembers(@PathVariable UUID id) {
        var members = taskMemberService.getTaskMembers(id);
        return ResponseEntity.ok(ApiMessageDTO.<List<TaskMemberDTO>>builder()
                .data(members)
                .result(true)
                .message(messageService.getMessage("members.found"))
                .code(HttpStatus.OK.value())
                .build());
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
    public ResponseEntity<ApiMessageDTO<List<TaskRequirementDTO>>> getRequirements(@PathVariable UUID id) {
        var requirements = taskRequirementService.getByTaskId(id);
        return ResponseEntity.ok(ApiMessageDTO.<List<TaskRequirementDTO>>builder()
                .data(requirements)
                .result(true)
                .message(messageService.getMessage("requirements.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Find tasks by project ID")
    @ApiOkResponse(value = TaskDTO.class)
    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<ApiMessageDTO<List<TaskDTO>>> findByProjectId(@PathVariable UUID projectId) {
        var tasks = taskQueryService.findByProjectId(projectId);
        return ResponseEntity.ok(ApiMessageDTO.<List<TaskDTO>>builder()
                .data(tasks)
                .result(true)
                .message(messageService.getMessage("tasks.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Find tasks by project ID with pagination")
    @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
    @GetMapping("/by-project/{projectId}/pagination")
    public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> findByProjectIdPagination(
            @PathVariable UUID projectId,
            Pageable pageable,
            PagedResourcesAssembler<TaskDTO> assembler) {
        var tasks = taskQueryService.findByProjectId(projectId, pageable);
        var pagedModel = assembler.toModel(tasks, this::toModel);
        return ResponseEntity.ok(ApiMessageDTO.<PagedModel<TaskDTO>>builder()
                .data(pagedModel)
                .result(true)
                .message(messageService.getMessage("tasks.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Find tasks by task list ID")
    @ApiOkResponse(value = TaskDTO.class)
    @GetMapping("/by-task-list/{taskListId}")
    public ResponseEntity<ApiMessageDTO<List<TaskDTO>>> findByTaskListId(@PathVariable UUID taskListId) {
        var tasks = taskQueryService.findByTaskListId(taskListId);
        return ResponseEntity.ok(ApiMessageDTO.<List<TaskDTO>>builder()
                .data(tasks)
                .result(true)
                .message(messageService.getMessage("tasks.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Find tasks by task list ID with pagination")
    @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
    @GetMapping("/by-task-list/{taskListId}/pagination")
    public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> findByTaskListIdPagination(
            @PathVariable UUID taskListId,
            Pageable pageable,
            PagedResourcesAssembler<TaskDTO> assembler) {
        var tasks = taskQueryService.findByTaskListId(taskListId, pageable);
        var pagedModel = assembler.toModel(tasks, this::toModel);
        return ResponseEntity.ok(ApiMessageDTO.<PagedModel<TaskDTO>>builder()
                .data(pagedModel)
                .result(true)
                .message(messageService.getMessage("tasks.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Find tasks accessible by current user")
    @ApiOkResponse(value = TaskDTO.class)
    @GetMapping("/my-tasks")
    public ResponseEntity<ApiMessageDTO<List<TaskDTO>>> findMyTasks() {
        var tasks = taskQueryService.findAccessibleByUser();
        return ResponseEntity.ok(ApiMessageDTO.<List<TaskDTO>>builder()
                .data(tasks)
                .result(true)
                .message(messageService.getMessage("tasks.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Find tasks accessible by current user with pagination")
    @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
    @GetMapping("/my-tasks/pagination")
    public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> findMyTasksPagination(
            Pageable pageable,
            PagedResourcesAssembler<TaskDTO> assembler) {
        var tasks = taskQueryService.findAccessibleByUser(pageable);
        var pagedModel = assembler.toModel(tasks, this::toModel);
        return ResponseEntity.ok(ApiMessageDTO.<PagedModel<TaskDTO>>builder()
                .data(pagedModel)
                .result(true)
                .message(messageService.getMessage("tasks.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Find tasks by status")
    @ApiOkResponse(value = TaskDTO.class)
    @GetMapping("/by-status")
    public ResponseEntity<ApiMessageDTO<List<TaskDTO>>> findByStatus(@RequestParam TaskStatus status) {
        var tasks = taskQueryService.findByStatus(status);
        return ResponseEntity.ok(ApiMessageDTO.<List<TaskDTO>>builder()
                .data(tasks)
                .result(true)
                .message(messageService.getMessage("tasks.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Find tasks by status with pagination")
    @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
    @GetMapping("/by-status/pagination")
    public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> findByStatusPagination(
            @RequestParam TaskStatus status,
            Pageable pageable,
            PagedResourcesAssembler<TaskDTO> assembler) {
        var tasks = taskQueryService.findByStatus(status, pageable);
        var pagedModel = assembler.toModel(tasks, this::toModel);
        return ResponseEntity.ok(ApiMessageDTO.<PagedModel<TaskDTO>>builder()
                .data(pagedModel)
                .result(true)
                .message(messageService.getMessage("tasks.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Search tasks created by current user")
    @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
    @PostMapping("/created-by-me/search")
    public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> searchTasksCreatedByMe(
            @RequestBody(required = false) SearchRequest searchRequest,
            PagedResourcesAssembler<TaskDTO> assembler) {
        var tasks = taskQueryService.searchTasksCreatedByMe(searchRequest);
        var pagedModel = assembler.toModel(tasks, this::toModel);
        return ResponseEntity.ok(ApiMessageDTO.<PagedModel<TaskDTO>>builder()
                .data(pagedModel)
                .result(true)
                .message(messageService.getMessage("tasks.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Search tasks assigned to current user")
    @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
    @PostMapping("/assigned-to-me/search")
    public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> searchTasksAssignedToMe(
            @RequestBody(required = false) SearchRequest searchRequest,
            PagedResourcesAssembler<TaskDTO> assembler) {
        var tasks = taskQueryService.searchTasksAssignedToMe(searchRequest);
        var pagedModel = assembler.toModel(tasks, this::toModel);
        return ResponseEntity.ok(ApiMessageDTO.<PagedModel<TaskDTO>>builder()
                .data(pagedModel)
                .result(true)
                .message(messageService.getMessage("tasks.found"))
                .code(HttpStatus.OK.value())
                .build());
    }
}
