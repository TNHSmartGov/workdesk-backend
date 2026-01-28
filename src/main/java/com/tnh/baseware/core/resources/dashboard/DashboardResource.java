package com.tnh.baseware.core.resources.dashboard;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.dtos.task.ActivityLogDTO;
import com.tnh.baseware.core.dtos.task.TaskStatisticDTO;
import com.tnh.baseware.core.dtos.dashboard.UnitPerformanceDTO;
import com.tnh.baseware.core.dtos.dashboard.UnitWorkloadDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.enums.ApiResponseType;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.dashboard.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.hateoas.EntityModel;
import com.tnh.baseware.core.dtos.task.CalendarTaskDTO;
import java.util.List;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Dashboard", description = "Dashboard Aggregation APIs")
public class DashboardResource {

        IDashboardService dashboardService;
        MessageService messageService;
        com.tnh.baseware.core.utils.SecurityUtils securityUtils;

        @Operation(summary = "Get personal statistics")
        @ApiOkResponse(value = TaskStatisticDTO.class)
        @GetMapping("/personal/statistics")
        public ResponseEntity<ApiMessageDTO<TaskStatisticDTO>> getPersonalStatistics(
                        @RequestParam(required = false) java.time.Instant fromDate,
                        @RequestParam(required = false) java.time.Instant toDate) {
                var userId = securityUtils.currentUser().getId();
                var orgId = securityUtils.currentOrgId();
                var data = dashboardService.getPersonalStatistics(userId, orgId, fromDate, toDate);
                return ResponseEntity.ok(ApiMessageDTO.<TaskStatisticDTO>builder()
                                .data(data)
                                .result(true)
                                .message(messageService.getMessage("dashboard.statistics.fetched"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get personal recent activities")
        @ApiOkResponse(value = ActivityLogDTO.class, type = ApiResponseType.HATEOAS_PAGE)
        @GetMapping("/personal/activities")
        public ResponseEntity<ApiMessageDTO<PagedModel<EntityModel<ActivityLogDTO>>>> getRecentActivities(
                        Pageable pageable,
                        PagedResourcesAssembler<ActivityLogDTO> assembler) {
                var activities = dashboardService.getRecentActivities(pageable);
                var pagedModel = assembler.toModel(activities);
                return ResponseEntity.ok(ApiMessageDTO.<PagedModel<EntityModel<ActivityLogDTO>>>builder()
                                .data(pagedModel)
                                .result(true)
                                .message(messageService.getMessage("dashboard.activities.fetched"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get calendar tasks")
        @ApiOkResponse(value = CalendarTaskDTO.class)
        @GetMapping("/calendar")
        public ResponseEntity<ApiMessageDTO<List<CalendarTaskDTO>>> getCalendarTasks(
                        @RequestParam Instant start,
                        @RequestParam Instant end) {
                var tasks = dashboardService.getCalendarTasks(start, end);
                return ResponseEntity.ok(ApiMessageDTO.<List<CalendarTaskDTO>>builder()
                                .data(tasks)
                                .result(true)
                                .message(messageService.getMessage("dashboard.calendar.fetched"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get unit performance statistics")
        @ApiOkResponse(value = UnitPerformanceDTO.class)
        @GetMapping("/unit/performance")
        public ResponseEntity<ApiMessageDTO<UnitPerformanceDTO>> getUnitPerformance() {
                var orgId = securityUtils.currentOrgId();
                var stats = dashboardService.getUnitPerformance(orgId);
                return ResponseEntity.ok(ApiMessageDTO.<UnitPerformanceDTO>builder()
                                .data(stats)
                                .result(true)
                                .message(messageService.getMessage("dashboard.unit.performance.fetched"))
                                .code(HttpStatus.OK.value())
                                .build());
        }

        @Operation(summary = "Get unit workload distribution")
        @ApiOkResponse(value = UnitWorkloadDTO.class)
        @GetMapping("/unit/workload")
        public ResponseEntity<ApiMessageDTO<List<UnitWorkloadDTO>>> getUnitWorkload() {
                var orgId = securityUtils.currentOrgId();
                var workload = dashboardService.getUnitWorkload(orgId);
                return ResponseEntity.ok(ApiMessageDTO.<List<UnitWorkloadDTO>>builder()
                                .data(workload)
                                .result(true)
                                .message(messageService.getMessage("dashboard.unit.workload.fetched"))
                                .code(HttpStatus.OK.value())
                                .build());
        }
}
