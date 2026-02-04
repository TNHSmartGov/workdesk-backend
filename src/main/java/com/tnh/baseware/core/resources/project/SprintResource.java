package com.tnh.baseware.core.resources.project;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.dtos.project.SprintDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.project.Sprint;
import com.tnh.baseware.core.enums.ApiResponseType;
import com.tnh.baseware.core.forms.project.SprintEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.ISprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Sprints", description = "API for managing sprints")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/sprints")
public class SprintResource extends GenericResource<Sprint, SprintEditorForm, SprintDTO, UUID> {

    ISprintService sprintService;

    public SprintResource(IGenericService<Sprint, SprintEditorForm, SprintDTO, UUID> service,
            MessageService messageService, SystemProperties systemProperties, ISprintService sprintService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/sprints");
        this.sprintService = sprintService;
    }

    @Operation(summary = "Get all sprints by project")
    @ApiOkResponse(value = SprintDTO.class, type = ApiResponseType.LIST)
    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<ApiMessageDTO<List<SprintDTO>>> findAllByProject(@PathVariable UUID projectId) {
        List<SprintDTO> sprints = sprintService.findAllByProjectId(projectId);
        return ResponseEntity.ok(ApiMessageDTO.<List<SprintDTO>>builder()
                .data(sprints)
                .result(true)
                .message(messageService.getMessage("sprints.retrieved"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Start a sprint")
    @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
    @PostMapping("/{id}/start")
    public ResponseEntity<ApiMessageDTO<Integer>> startSprint(@PathVariable UUID id) {
        sprintService.startSprint(id);
        return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                .data(1)
                .result(true)
                .message(messageService.getMessage("sprint.started"))
                .code(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Complete a sprint")
    @ApiOkResponse(value = Integer.class, type = ApiResponseType.OBJECT)
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiMessageDTO<Integer>> completeSprint(@PathVariable UUID id) {
        sprintService.completeSprint(id);
        return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                .data(1)
                .result(true)
                .message(messageService.getMessage("sprint.completed"))
                .code(HttpStatus.OK.value())
                .build());
    }
}
