package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.dtos.task.TaskAgileInfoDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.task.TaskAgileInfo;
import com.tnh.baseware.core.enums.ApiResponseType;
import com.tnh.baseware.core.forms.task.TaskAgileInfoEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskAgileInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Task Agile Info", description = "API for managing task agile details")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-agile-info")
public class TaskAgileInfoResource
        extends GenericResource<TaskAgileInfo, TaskAgileInfoEditorForm, TaskAgileInfoDTO, UUID> {

    ITaskAgileInfoService taskAgileInfoService;

    public TaskAgileInfoResource(
            IGenericService<TaskAgileInfo, TaskAgileInfoEditorForm, TaskAgileInfoDTO, UUID> service,
            MessageService messageService, SystemProperties systemProperties,
            ITaskAgileInfoService taskAgileInfoService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-agile-info");
        this.taskAgileInfoService = taskAgileInfoService;
    }

    @Operation(summary = "Get agile info by task ID")
    @ApiOkResponse(value = TaskAgileInfoDTO.class, type = ApiResponseType.OBJECT)
    @GetMapping("/by-task/{taskId}")
    public ResponseEntity<ApiMessageDTO<TaskAgileInfoDTO>> findByTaskId(@PathVariable UUID taskId) {
        TaskAgileInfoDTO dto = taskAgileInfoService.findByTaskId(taskId);
        return ResponseEntity.ok(ApiMessageDTO.<TaskAgileInfoDTO>builder()
                .data(dto)
                .result(true)
                .message(messageService.getMessage("task.agile.info.retrieved"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Update agile info by task ID")
    @ApiOkResponse(value = TaskAgileInfoDTO.class, type = ApiResponseType.OBJECT)
    @PutMapping("/by-task/{taskId}")
    public ResponseEntity<ApiMessageDTO<TaskAgileInfoDTO>> updateByTaskId(@PathVariable UUID taskId,
            @RequestBody TaskAgileInfoEditorForm form) {
        TaskAgileInfoDTO dto = taskAgileInfoService.updateByTaskId(taskId, form);
        return ResponseEntity.ok(ApiMessageDTO.<TaskAgileInfoDTO>builder()
                .data(dto)
                .result(true)
                .message(messageService.getMessage("task.agile.info.updated"))
                .code(HttpStatus.OK.value())
                .build());
    }
}
