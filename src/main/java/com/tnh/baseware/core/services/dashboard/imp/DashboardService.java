package com.tnh.baseware.core.services.dashboard.imp;

import com.tnh.baseware.core.dtos.task.ActivityLogDTO;
import com.tnh.baseware.core.dtos.task.TaskStatisticDTO;
import com.tnh.baseware.core.dtos.dashboard.UnitPerformanceDTO;
import com.tnh.baseware.core.dtos.dashboard.UnitWorkloadDTO;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.services.dashboard.IDashboardService;
import com.tnh.baseware.core.repositories.user.IUserOrganizationRepository;
import com.tnh.baseware.core.enums.TitleDefault;
import com.tnh.baseware.core.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public TaskStatisticDTO getPersonalStatistics(Instant from, Instant to) {
        UUID orgId = securityUtils.currentOrgId();
        UUID userId = securityUtils.currentUser().getId();
        Instant now = Instant.now();
        Instant future = now.plus(3, ChronoUnit.DAYS);

        TaskStatisticDTO stats = new TaskStatisticDTO();
        if (isUnitManager(userId, orgId)) {
            stats.setTotal(taskRepository.countByOrganizationIdTimeboxed(orgId, from, to));
            stats.setTotalNew(taskRepository.countByOrganizationIdAndStatusTimeboxed(orgId, TaskStatus.TODO, from, to));
            stats.setTotalInProgress(
                    taskRepository.countByOrganizationIdAndStatusTimeboxed(orgId, TaskStatus.IN_PROGRESS, from, to));
            stats.setTotalReview(
                    taskRepository.countByOrganizationIdAndStatusTimeboxed(orgId, TaskStatus.REVIEW, from, to));

            // Logic: Completed tasks -> Filter by Completion Date (ModifiedDate)
            stats.setTotalCompleted(
                    taskRepository.countByOrganizationIdAndStatusFinishedTimeboxed(orgId, TaskStatus.DONE, from, to));

            // Overdue/DueSoon are "Snapshot" metrics, usually ignore "Created Date" filter,
            // but enforce "Active now".
            // If user wants "Overdue tasks among those created this month", we use the
            // filter.
            // Let's assume filter applies to the *set of tasks considered*.
            // Currently Overdue query does not support timeboxing createdDate easily
            // without modifying it.
            // For now, keep Overdue/DueSoon as "Current State" (ignoring from/to) or we
            // need to add Timebox to them too.
            // User request usually implies filtering the "Report".
            // Let's leave Overdue/DueSoon as global snapshot for now unless requested
            // otherwise.
            stats.setTotalOverdue(taskRepository.countByOrganizationIdOverdue(orgId, now));
            stats.setTotalDueSoon(taskRepository.countByOrganizationIdDueSoon(orgId, now, future));
        } else {
            stats.setTotal(taskRepository.countAccessibleByUserTimeboxed(orgId, userId, from, to));
            stats.setTotalNew(
                    taskRepository.countAccessibleByStatusTimeboxed(orgId, userId, TaskStatus.TODO, from, to));
            stats.setTotalInProgress(
                    taskRepository.countAccessibleByStatusTimeboxed(orgId, userId, TaskStatus.IN_PROGRESS, from, to));
            stats.setTotalReview(
                    taskRepository.countAccessibleByStatusTimeboxed(orgId, userId, TaskStatus.REVIEW, from, to));

            // Logic: Completed tasks -> Filter by Completion Date (ModifiedDate)
            stats.setTotalCompleted(
                    taskRepository.countAccessibleByStatusFinishedTimeboxed(orgId, userId, TaskStatus.DONE, from, to));

            stats.setTotalOverdue(taskRepository.countAccessibleOverdue(orgId, userId, now));
            stats.setTotalDueSoon(taskRepository.countAccessibleDueSoon(orgId, userId, now, future));
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
                .projectId(log.getTask().getProject() != null ? log.getTask().getProject().getId() : null)
                .projectName(log.getTask().getProject() != null ? log.getTask().getProject().getName() : null)
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
    public java.util.List<com.tnh.baseware.core.dtos.task.CalendarTaskDTO> getCalendarTasks(Instant startDate,
            Instant endDate) {
        UUID orgId = securityUtils.currentOrgId();
        UUID userId = securityUtils.currentUser().getId();

        return taskRepository.findAccessibleByDueDateRange(orgId, userId, startDate, endDate).stream()
                .map(task -> com.tnh.baseware.core.dtos.task.CalendarTaskDTO.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .startDate(task.getStartDate())
                        .dueDate(task.getDueDate())
                        .status(task.getStatus())
                        .priority(task.getPriority())
                        .projectId(task.getProject() != null ? task.getProject().getId() : null)
                        .projectName(task.getProject() != null ? task.getProject().getName() : null)
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UnitPerformanceDTO getUnitPerformance() {
        UUID orgId = securityUtils.currentOrgId();
        // Default logic: User's organization

        long total = taskRepository.countByOrganizationId(orgId);
        long completed = taskRepository.countByOrganizationIdAndStatus(orgId, TaskStatus.DONE);
        long overdue = taskRepository.countByOrganizationIdOverdue(orgId, Instant.now());

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
    public java.util.List<UnitWorkloadDTO> getUnitWorkload() {
        UUID orgId = securityUtils.currentOrgId();
        return taskMemberRepository.getWorkloadDistribution(orgId, TaskMemberRole.ASSIGNEE, Instant.now(),
                TaskStatus.DONE);
    }

    private boolean isUnitManager(UUID userId, UUID orgId) {
        return userOrganizationRepository.findByUserIdAndOrganizationId(userId, orgId)
                .map(uo -> uo.getTitle() != null
                        && (TitleDefault.UNIT_LEADER.getValue().equals(uo.getTitle().getName())
                                || TitleDefault.DEPUTY.getValue().equals(uo.getTitle().getName())))
                .orElse(false);
    }
}
