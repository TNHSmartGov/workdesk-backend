package com.tnh.baseware.core.resources.dashboard;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.dtos.dashboard.executive.*;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.dashboard.IExecutiveDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/executive")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Executive Dashboard", description = "High-level metrics for leaders")
public class ExecutiveDashboardResource {

        IExecutiveDashboardService executiveService;
        MessageService messageService;
        com.tnh.baseware.core.utils.SecurityUtils securityUtils;

        @Operation(summary = "Get risk hotspots (Overdue, At Risk, Blocked)")
        @ApiOkResponse(value = ExecutiveHotspotDTO.class)
        @GetMapping("/hotspots")
        public ResponseEntity<ApiMessageDTO<ExecutiveHotspotDTO>> getHotspots(
                        @RequestParam(required = false) java.time.Instant fromDate,
                        @RequestParam(required = false) java.time.Instant toDate) {
                var orgId = securityUtils.currentOrgId();
                var data = executiveService.getHotspots(orgId, fromDate, toDate);
                return ResponseEntity.ok(ApiMessageDTO.<ExecutiveHotspotDTO>builder()
                                .data(data)
                                .result(true)
                                .message(messageService.getMessage("dashboard.hotspots.fetched"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get actionable items (Approval Queue, My Urgent Tasks)")
        @ApiOkResponse(value = ExecutiveActionItemDTO.class)
        @GetMapping("/action-items")
        public ResponseEntity<ApiMessageDTO<ExecutiveActionItemDTO>> getActionItems(
                        @RequestParam(required = false) java.time.Instant fromDate,
                        @RequestParam(required = false) java.time.Instant toDate) {
                var orgId = securityUtils.currentOrgId();
                var userId = securityUtils.currentUser().getId();
                var data = executiveService.getActionItems(orgId, userId, fromDate, toDate);
                return ResponseEntity.ok(ApiMessageDTO.<ExecutiveActionItemDTO>builder()
                                .data(data)
                                .result(true)
                                .message(messageService.getMessage("dashboard.actions.fetched"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get team velocity (Last 4 weeks)")
        @ApiOkResponse(value = WeekVelocityDTO.class)
        @GetMapping("/velocity")
        public ResponseEntity<ApiMessageDTO<List<WeekVelocityDTO>>> getVelocity(
                        @RequestParam(required = false) java.time.Instant fromDate,
                        @RequestParam(required = false) java.time.Instant toDate) {
                var orgId = securityUtils.currentOrgId();
                var data = executiveService.getVelocity(orgId, fromDate, toDate);
                return ResponseEntity.ok(ApiMessageDTO.<List<WeekVelocityDTO>>builder()
                                .data(data)
                                .result(true)
                                .message(messageService.getMessage("dashboard.velocity.fetched"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get resource health (Overload detection)")
        @ApiOkResponse(value = ResourceHealthDTO.class)
        @GetMapping("/resources/health")
        public ResponseEntity<ApiMessageDTO<List<ResourceHealthDTO>>> getResourceHealth(
                        @RequestParam(required = false) java.time.Instant fromDate,
                        @RequestParam(required = false) java.time.Instant toDate) {
                var orgId = securityUtils.currentOrgId();
                var data = executiveService.getResourceHealth(orgId, fromDate, toDate);
                return ResponseEntity.ok(ApiMessageDTO.<List<ResourceHealthDTO>>builder()
                                .data(data)
                                .result(true)
                                .message(messageService.getMessage("dashboard.resources.fetched"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get project health (Progress of active projects)")
        @ApiOkResponse(value = ProjectHealthDTO.class)
        @GetMapping("/projects/health")
        public ResponseEntity<ApiMessageDTO<List<ProjectHealthDTO>>> getProjectHealth(
                        @RequestParam(required = false) java.time.Instant fromDate,
                        @RequestParam(required = false) java.time.Instant toDate) {
                var orgId = securityUtils.currentOrgId();
                var data = executiveService.getProjectHealth(orgId, fromDate, toDate);
                return ResponseEntity.ok(ApiMessageDTO.<List<ProjectHealthDTO>>builder()
                                .data(data)
                                .result(true)
                                .message(messageService.getMessage("dashboard.projects.fetched"))
                                .code(HttpStatus.OK.value())
                                .build());
        }
}
