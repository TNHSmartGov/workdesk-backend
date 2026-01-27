package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.dtos.task.TaskMemberDTO;
import com.tnh.baseware.core.dtos.task.TaskRequirementDTO;
import com.tnh.baseware.core.dtos.task.TaskTimelineItemDTO;

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
        public ResponseEntity<ApiMessageDTO<String>> performAction(@PathVariable UUID id,
                        @RequestBody @Valid TaskActionForm form) {
                taskCommandService.performAction(id, form.getAction());
                return ResponseEntity.ok(ApiMessageDTO.<String>builder()
                                .data("Action performed successfully")
                                .result(true)
                                .message(messageService.getMessage("task.action.performed"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Update personal progress")
        @PatchMapping("/{id}/progress")
        public ResponseEntity<ApiMessageDTO<String>> updateProgress(@PathVariable UUID id,
                        @RequestBody @Valid UpdateProgressForm form) {
                taskCommandService.updatePersonalProgress(id, form.getProgress());
                return ResponseEntity.ok(ApiMessageDTO.<String>builder()
                                .data("Progress updated successfully")
                                .result(true)
                                .message(messageService.getMessage("task.progress.updated"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Report task progress with comment and attachments")
        @PostMapping("/{id}/reports")
        public ResponseEntity<ApiMessageDTO<String>> reportProgress(@PathVariable UUID id,
                        @RequestBody @Valid CreateTaskReportForm form) {
                taskCommandService.reportProgress(id, form);
                return ResponseEntity.ok(ApiMessageDTO.<String>builder()
                                .data("Report submitted successfully")
                                .result(true)
                                .message(messageService.getMessage("task.report.submitted"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Assign members to task")
        @PostMapping("/{id}/members")
        @ApiOkResponse(value = TaskMemberDTO.class)
        public ResponseEntity<ApiMessageDTO<List<TaskMemberDTO>>> assignMembers(@PathVariable UUID id,
                        @RequestBody List<TaskMemberEditorForm> forms) {
                var members = taskMemberService.assignMembers(id, forms);
                return ResponseEntity.ok(ApiMessageDTO.<List<TaskMemberDTO>>builder()
                                .data(members)
                                .result(true)
                                .message(messageService.getMessage("task.members.assigned"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Update task member")
        @PutMapping("/{id}/members/{memberId}")
        @ApiOkResponse(value = TaskMemberDTO.class, type = ApiResponseType.OBJECT)
        public ResponseEntity<ApiMessageDTO<TaskMemberDTO>> updateMember(@PathVariable UUID id,
                        @PathVariable UUID memberId,
                        @RequestBody @Valid TaskMemberEditorForm form) {
                var member = taskMemberService.updateMember(id, memberId, form);
                return ResponseEntity.ok(ApiMessageDTO.<TaskMemberDTO>builder()
                                .data(member)
                                .result(true)
                                .message(messageService.getMessage("task.member.updated"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Remove member from task")
        @DeleteMapping("/{id}/members/{memberId}")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        public ResponseEntity<ApiMessageDTO<Integer>> removeMember(@PathVariable UUID id, @PathVariable UUID memberId) {
                taskMemberService.removeMember(id, memberId);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("task.member.removed"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get task members")
        @GetMapping("/{id}/members")
        @ApiOkResponse(value = TaskMemberDTO.class)
        public ResponseEntity<ApiMessageDTO<List<TaskMemberDTO>>> getMembers(@PathVariable UUID id) {
                var members = taskMemberService.getTaskMembers(id);
                return ResponseEntity.ok(ApiMessageDTO.<List<TaskMemberDTO>>builder()
                                .data(members)
                                .result(true)
                                .message(messageService.getMessage("task.members.retrieved"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Add requirement to task")
        @PostMapping("/{id}/requirements")
        @ApiOkResponse(value = TaskRequirementDTO.class, type = ApiResponseType.OBJECT)
        public ResponseEntity<ApiMessageDTO<TaskRequirementDTO>> addRequirement(@PathVariable UUID id,
                        @RequestBody @Valid TaskRequirementEditorForm form) {
                var requirement = taskRequirementService.create(id, form);
                return ResponseEntity.ok(ApiMessageDTO.<TaskRequirementDTO>builder()
                                .data(requirement)
                                .result(true)
                                .message(messageService.getMessage("task.requirement.added"))
                                .code(HttpStatus.CREATED.value())
                                .build());
        }

        @Operation(summary = "Update task requirement")
        @PutMapping("/{id}/requirements/{requirementId}")
        @ApiOkResponse(value = TaskRequirementDTO.class, type = ApiResponseType.OBJECT)
        public ResponseEntity<ApiMessageDTO<TaskRequirementDTO>> updateRequirement(@PathVariable UUID id,
                        @PathVariable UUID requirementId,
                        @RequestBody @Valid TaskRequirementEditorForm form) {
                var requirement = taskRequirementService.update(id, requirementId, form);
                return ResponseEntity.ok(ApiMessageDTO.<TaskRequirementDTO>builder()
                                .data(requirement)
                                .result(true)
                                .message(messageService.getMessage("task.requirement.updated"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Delete task requirement")
        @DeleteMapping("/{id}/requirements/{requirementId}")
        @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
        public ResponseEntity<ApiMessageDTO<Integer>> deleteRequirement(@PathVariable UUID id,
                        @PathVariable UUID requirementId) {
                taskRequirementService.delete(id, requirementId);
                return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                                .data(1)
                                .result(true)
                                .message(messageService.getMessage("task.requirement.deleted"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Assign task requirement to member")
        @PatchMapping("/{id}/requirements/{requirementId}/assign")
        @ApiOkResponse(value = TaskRequirementDTO.class, type = ApiResponseType.OBJECT)
        public ResponseEntity<ApiMessageDTO<TaskRequirementDTO>> assignRequirement(@PathVariable UUID id,
                        @PathVariable UUID requirementId,
                        @RequestBody @Valid AssignRequirementForm form) {
                var requirement = taskRequirementService.assignToMember(id, requirementId, form);
                return ResponseEntity.ok(ApiMessageDTO.<TaskRequirementDTO>builder()
                                .data(requirement)
                                .result(true)
                                .message(messageService.getMessage("task.requirement.assigned"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Toggle task requirement complete status")
        @PatchMapping("/{id}/requirements/{requirementId}/toggle")
        @ApiOkResponse(value = String.class, type = ApiResponseType.OBJECT)
        public ResponseEntity<ApiMessageDTO<String>> toggleRequirement(@PathVariable UUID id,
                        @PathVariable UUID requirementId) {
                taskRequirementService.toggleComplete(id, requirementId);
                return ResponseEntity.ok(ApiMessageDTO.<String>builder()
                                .data("Requirement toggled successfully")
                                .result(true)
                                .message(messageService.getMessage("task.requirement.toggled"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get task requirements")
        @GetMapping("/{id}/requirements")
        @ApiOkResponse(value = TaskRequirementDTO.class)
        public ResponseEntity<ApiMessageDTO<List<TaskRequirementDTO>>> getRequirements(@PathVariable UUID id) {
                var requirements = taskRequirementService.getByTaskId(id);
                return ResponseEntity.ok(ApiMessageDTO.<List<TaskRequirementDTO>>builder()
                                .data(requirements)
                                .result(true)
                                .message(messageService.getMessage("task.requirements.retrieved"))
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

        @Operation(summary = "Get task timeline (Activity Log + Comments)")
        @GetMapping("/{id}/timeline")
        public ResponseEntity<ApiMessageDTO<List<TaskTimelineItemDTO>>> getTaskTimeline(
                        @PathVariable UUID id) {
                var timeline = taskQueryService.getTaskTimeline(id);
                return ResponseEntity.ok(ApiMessageDTO.<List<TaskTimelineItemDTO>>builder()
                                .data(timeline)
                                .result(true)
                                .message(messageService.getMessage("timeline.found"))
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
                                .message(messageService.getMessage("timeline.found"))
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

        @Operation(summary = "Search accessible tasks by current user")
        @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
        @PostMapping("/my-tasks/search")
        public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> searchMyTasks(
                        @RequestBody(required = false) SearchRequest searchRequest,
                        PagedResourcesAssembler<TaskDTO> assembler) {
                var tasks = taskQueryService.searchAccessibleByUser(searchRequest);
                var pagedModel = assembler.toModel(tasks, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<TaskDTO>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("tasks.found"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Search accessible tasks by current user with pagination")
        @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
        @PostMapping("/my-tasks/pagination/search")
        public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> searchMyTasksPagination(
                        @RequestBody(required = false) SearchRequest searchRequest,
                        PagedResourcesAssembler<TaskDTO> assembler) {
                return searchMyTasks(searchRequest, assembler);
        }

        @Operation(summary = "Search tasks by project ID")
        @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
        @PostMapping("/by-project/{projectId}/search")
        public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> searchByProjectId(
                        @PathVariable UUID projectId,
                        @RequestBody(required = false) SearchRequest searchRequest,
                        PagedResourcesAssembler<TaskDTO> assembler) {
                var tasks = taskQueryService.searchByProjectId(projectId, searchRequest);
                var pagedModel = assembler.toModel(tasks, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<TaskDTO>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("tasks.found"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Search tasks by task list ID")
        @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
        @PostMapping("/by-task-list/{taskListId}/search")
        public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> searchByTaskListId(
                        @PathVariable UUID taskListId,
                        @RequestBody(required = false) SearchRequest searchRequest,
                        PagedResourcesAssembler<TaskDTO> assembler) {
                var tasks = taskQueryService.searchByTaskListId(taskListId, searchRequest);
                var pagedModel = assembler.toModel(tasks, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<TaskDTO>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("tasks.found"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Search tasks by status")
        @ApiOkResponse(value = TaskDTO.class, type = ApiResponseType.HATEOAS_PAGE)
        @PostMapping("/by-status/search")
        public ResponseEntity<ApiMessageDTO<PagedModel<TaskDTO>>> searchByStatus(
                        @RequestParam TaskStatus status,
                        @RequestBody(required = false) SearchRequest searchRequest,
                        PagedResourcesAssembler<TaskDTO> assembler) {
                var tasks = taskQueryService.searchByStatus(status, searchRequest);
                var pagedModel = assembler.toModel(tasks, this::toModel);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<TaskDTO>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("tasks.found"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

}
