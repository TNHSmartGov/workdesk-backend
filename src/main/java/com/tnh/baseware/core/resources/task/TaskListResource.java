package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.dtos.task.TaskListDTO;
import com.tnh.baseware.core.dtos.task.TaskListProjectDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.task.TaskList;
import com.tnh.baseware.core.enums.ApiResponseType;
import com.tnh.baseware.core.forms.task.TaskListEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskListService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    @Operation(summary = "Get task lists by project id")
    @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<ApiMessageDTO<List<TaskListProjectDTO>>> getTaskListsByProjectId(
            @PathVariable UUID projectId) {
        List<TaskListProjectDTO> taskList = taskListService.getTaskListsByProjectId(projectId);
        ApiMessageDTO<List<TaskListProjectDTO>> apiMessageDTO = ApiMessageDTO.<List<TaskListProjectDTO>>builder()
                .data(taskList)
                .result(true)
                .message(messageService.getMessage("task-lists.fetched"))
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(apiMessageDTO);
    }

}
