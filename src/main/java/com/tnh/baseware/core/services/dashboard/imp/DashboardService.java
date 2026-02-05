package com.tnh.baseware.core.services.dashboard.imp;

import com.tnh.baseware.core.dtos.task.ActivityLogDTO;
import com.tnh.baseware.core.dtos.task.CalendarTaskDTO;
import com.tnh.baseware.core.dtos.task.TaskStatisticDTO;
import com.tnh.baseware.core.dtos.dashboard.UnitPerformanceDTO;
import com.tnh.baseware.core.dtos.dashboard.UnitWorkloadDTO;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.stats.IOrganizationDailyStatsRepository;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.services.dashboard.IDashboardService;
import com.tnh.baseware.core.repositories.user.IUserOrganizationRepository;
import com.tnh.baseware.core.enums.OrganizationRole;
import com.tnh.baseware.core.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardService implements IDashboardService {

        ITaskRepository taskRepository;
        IProjectRepository projectRepository;
        ITaskActivityLogRepository activityLogRepository;
        IUserOrganizationRepository userOrganizationRepository;
        ITaskMemberRepository taskMemberRepository;
        IOrganizationDailyStatsRepository dailyStatsRepository;
        SecurityUtils securityUtils;

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "dashboard_personal", key = "{#userId, #orgId, #from, #to}")
        public TaskStatisticDTO getPersonalStatistics(UUID userId, UUID orgId, Instant from, Instant to) {
                Instant now = Instant.now();
                Instant future = now.plus(3, ChronoUnit.DAYS);

                // Sanitize Dates to avoid NULL parameters in JDBC
                Instant safeFrom = from != null ? from : Instant.EPOCH; // 1970-01-01
                Instant safeTo = to != null ? to : Instant.parse("2100-01-01T00:00:00Z");

                TaskStatisticDTO stats = new TaskStatisticDTO();
                if (isUnitManager(userId, orgId)) {
                        stats.setTotal(taskRepository.countByOrganizationIdTimeboxed(orgId, safeFrom, safeTo));
                        stats.setTotalNew(taskRepository.countByOrganizationIdAndStatusTimeboxed(orgId, TaskStatus.TODO,
                                        safeFrom, safeTo));
                        stats.setTotalInProgress(
                                        taskRepository.countByOrganizationIdAndStatusTimeboxed(orgId,
                                                        TaskStatus.IN_PROGRESS, safeFrom, safeTo));
                        stats.setTotalReview(
                                        taskRepository.countByOrganizationIdAndStatusTimeboxed(orgId, TaskStatus.REVIEW,
                                                        safeFrom, safeTo));

                        // Logic: Completed tasks -> Filter by Completion Date (ModifiedDate)
                        stats.setTotalCompleted(
                                        taskRepository.countByOrganizationIdAndStatusFinishedTimeboxed(orgId,
                                                        TaskStatus.DONE, safeFrom, safeTo));

                        stats.setTotalOverdue(taskRepository.countByOrganizationIdOverdueTimeboxed(orgId, now, safeFrom,
                                        safeTo));
                        stats.setTotalDueSoon(taskRepository.countByOrganizationIdDueSoonTimeboxed(orgId, now, future,
                                        safeFrom, safeTo));
                } else {
                        stats.setTotal(taskRepository.countAccessibleByUserTimeboxed(orgId, userId, safeFrom, safeTo));
                        stats.setTotalNew(
                                        taskRepository.countAccessibleByStatusTimeboxed(orgId, userId, TaskStatus.TODO,
                                                        safeFrom, safeTo));
                        stats.setTotalInProgress(
                                        taskRepository.countAccessibleByStatusTimeboxed(orgId, userId,
                                                        TaskStatus.IN_PROGRESS, safeFrom, safeTo));
                        stats.setTotalReview(
                                        taskRepository.countAccessibleByStatusTimeboxed(orgId, userId,
                                                        TaskStatus.REVIEW, safeFrom, safeTo));

                        // Logic: Completed tasks -> Filter by Completion Date (ModifiedDate)
                        stats.setTotalCompleted(
                                        taskRepository.countAccessibleByStatusFinishedTimeboxed(orgId, userId,
                                                        TaskStatus.DONE, safeFrom, safeTo));

                        stats.setTotalOverdue(taskRepository.countAccessibleOverdueTimeboxed(orgId, userId, now,
                                        safeFrom, safeTo));
                        stats.setTotalDueSoon(taskRepository.countAccessibleDueSoonTimeboxed(orgId, userId, now, future,
                                        safeFrom, safeTo));
                }

                // New Metric: Active Projects (Can also timebox by StartDate if needed, but
                // usually Active is state)
                stats.setTotalActiveProjects(projectRepository.countActiveProjectsByUser(orgId, userId));

                return stats;
        }

        @Override
        @Transactional(readOnly = true)
        public Page<ActivityLogDTO> getRecentActivities(Pageable pageable) {
                UUID orgId = securityUtils.currentOrgId();
                UUID userId = securityUtils.currentUser().getId();

                var logs = activityLogRepository.findAccessibleByUser(orgId, userId, pageable);

                return logs.map(log -> ActivityLogDTO.builder()
                                .id(log.getId())
                                .taskId(log.getTask().getId())
                                .taskTitle(log.getTask().getTitle())
                                .projectId(log.getTask().getProject() != null ? log.getTask().getProject().getId()
                                                : null)
                                .projectName(log.getTask().getProject() != null ? log.getTask().getProject().getName()
                                                : null)
                                .actorName(log.getActor().getFullName())
                                .actorAvatar(log.getActor().getAvatarUrl())
                                .actionType(log.getActionType())
                                .targetField(log.getTargetField())
                                .oldValue(log.getOldValue())
                                .newValue(log.getNewValue())
                                .timestamp(log.getCreatedDate())
                                .build());
        }

        @Override
        @Transactional(readOnly = true)
        public java.util.List<CalendarTaskDTO> getCalendarTasks(Instant startDate,
                        Instant endDate) {
                UUID orgId = securityUtils.currentOrgId();
                UUID userId = securityUtils.currentUser().getId();

                return taskRepository.findAccessibleByDueDateRange(orgId, userId, startDate, endDate).stream()
                                .map(task -> CalendarTaskDTO.builder()
                                                .id(task.getId())
                                                .title(task.getTitle())
                                                .startDate(task.getStartDate())
                                                .dueDate(task.getDueDate())
                                                .status(task.getStatus())
                                                .priority(task.getPriority())
                                                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                                                .projectName(task.getProject() != null ? task.getProject().getName()
                                                                : null)
                                                .build())
                                .collect(java.util.stream.Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "dashboard_unit", key = "{#orgId, #from, #to}")
        public UnitPerformanceDTO getUnitPerformance(UUID orgId, Instant from, Instant to) {
                Instant now = Instant.now();
                Instant safeFrom = from != null ? from : Instant.EPOCH;
                Instant safeTo = to != null ? to : Instant.parse("2100-01-01T00:00:00Z");

                // Determine "Today" start (UTC)
                Instant startOfToday = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant();

                // 1. Try Historical Stats if range is fully in the past
                if (safeTo.isBefore(startOfToday)) {
                        UnitPerformanceDTO stats = dailyStatsRepository.aggregatePerformance(orgId, safeFrom, safeTo);
                        if (stats != null && stats.getTotalTasksCreated() != null) {

                                return stats;
                        }
                }

                // 2. Fallback to Live Calculation (For Today, Future, or Missing Stats)
                // This preserves current behavior for "Current/Recent" views which is usually
                // acceptable.

                long total = taskRepository.countByOrganizationIdTimeboxed(orgId, safeFrom, safeTo);
                long completed = taskRepository.countByOrganizationIdAndStatusFinishedTimeboxed(orgId, TaskStatus.DONE,
                                safeFrom, safeTo);
                long overdue = taskRepository.countByOrganizationIdOverdueTimeboxed(orgId, now, safeFrom,
                                safeTo);

                double completionRate = total == 0 ? 0.0 : ((double) completed / total) * 100.0;
                double overdueRate = total == 0 ? 0.0 : ((double) overdue / total) * 100.0;

                return UnitPerformanceDTO.builder()
                                .totalTasksCreated(total)
                                .completionRate(Math.ceil(completionRate * 100) / 100)
                                .overdueRate(Math.ceil(overdueRate * 100) / 100)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "dashboard_unit", key = "'workload:' + #orgId + ':' + #from + ':' + #to")
        public java.util.List<UnitWorkloadDTO> getUnitWorkload(UUID orgId, Instant from, Instant to) {
                Instant safeFrom = from != null ? from : Instant.EPOCH;
                Instant safeTo = to != null ? to : Instant.parse("2100-01-01T00:00:00Z");
                return taskMemberRepository.getWorkloadDistributionTimeboxed(orgId,
                                List.of(TaskMemberRole.ASSIGNEE, TaskMemberRole.LEAD, TaskMemberRole.OWNER,
                                                TaskMemberRole.REVIEWER),
                                Instant.now(),
                                TaskStatus.DONE, safeFrom, safeTo);
        }

        private boolean isUnitManager(UUID userId, UUID orgId) {
                return userOrganizationRepository.findByUserIdAndOrganizationId(userId, orgId)
                                .map(uo -> {
                                        OrganizationRole role = uo.getOrganizationRole();
                                        return role == OrganizationRole.UNIT_LEADER || role == OrganizationRole.DEPUTY;
                                })
                                .orElse(false);
        }
}
