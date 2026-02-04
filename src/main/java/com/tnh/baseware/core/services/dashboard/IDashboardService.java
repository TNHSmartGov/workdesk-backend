package com.tnh.baseware.core.services.dashboard;

import com.tnh.baseware.core.dtos.task.ActivityLogDTO;
import com.tnh.baseware.core.dtos.task.CalendarTaskDTO;
import com.tnh.baseware.core.dtos.task.TaskStatisticDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface IDashboardService {
    /**
     * Get personal statistics for the current user.
     * Includes task counts (Total, New, Overdue) and active project count.
     */
    TaskStatisticDTO getPersonalStatistics(java.util.UUID userId, java.util.UUID orgId, java.time.Instant from,
            java.time.Instant to);

    /**
     * Get recent activity stream for the current user.
     * Aggregates logs from all accessible tasks/projects.
     */
    Page<ActivityLogDTO> getRecentActivities(Pageable pageable);

    /**
     * Get tasks for calendar view within a specific date range.
     */
    List<CalendarTaskDTO> getCalendarTasks(Instant startDate, Instant endDate);

    com.tnh.baseware.core.dtos.dashboard.UnitPerformanceDTO getUnitPerformance(java.util.UUID orgId,
            java.time.Instant from, java.time.Instant to);

    List<com.tnh.baseware.core.dtos.dashboard.UnitWorkloadDTO> getUnitWorkload(java.util.UUID orgId,
            java.time.Instant from, java.time.Instant to);
}
