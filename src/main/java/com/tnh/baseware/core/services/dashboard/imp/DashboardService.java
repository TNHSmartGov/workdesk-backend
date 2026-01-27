package com.tnh.baseware.core.services.dashboard.imp;

import com.tnh.baseware.core.dtos.task.ActivityLogDTO;
import com.tnh.baseware.core.dtos.task.TaskStatisticDTO;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
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
    SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public TaskStatisticDTO getPersonalStatistics() {
        UUID orgId = securityUtils.currentOrgId();
        UUID userId = securityUtils.currentUser().getId();
        Instant now = Instant.now();
        Instant future = now.plus(3, ChronoUnit.DAYS);

        TaskStatisticDTO stats = new TaskStatisticDTO();
        if (isUnitManager(userId, orgId)) {
            stats.setTotal(taskRepository.countByOrganizationId(orgId));
            stats.setTotalNew(taskRepository.countByOrganizationIdAndStatus(orgId, TaskStatus.TODO));
            stats.setTotalInProgress(taskRepository.countByOrganizationIdAndStatus(orgId, TaskStatus.IN_PROGRESS));
            stats.setTotalReview(taskRepository.countByOrganizationIdAndStatus(orgId, TaskStatus.REVIEW));
            stats.setTotalCompleted(taskRepository.countByOrganizationIdAndStatus(orgId, TaskStatus.DONE));
            stats.setTotalOverdue(taskRepository.countByOrganizationIdOverdue(orgId, now));
            stats.setTotalDueSoon(taskRepository.countByOrganizationIdDueSoon(orgId, now, future));
        } else {
            stats.setTotal(taskRepository.countAccessibleByUser(orgId, userId));
            stats.setTotalNew(taskRepository.countAccessibleByStatus(orgId, userId, TaskStatus.TODO));
            stats.setTotalInProgress(taskRepository.countAccessibleByStatus(orgId, userId, TaskStatus.IN_PROGRESS));
            stats.setTotalReview(taskRepository.countAccessibleByStatus(orgId, userId, TaskStatus.REVIEW));
            stats.setTotalCompleted(taskRepository.countAccessibleByStatus(orgId, userId, TaskStatus.DONE));
            stats.setTotalOverdue(taskRepository.countAccessibleOverdue(orgId, userId, now));
            stats.setTotalDueSoon(taskRepository.countAccessibleDueSoon(orgId, userId, now, future));
        }

        // New Metric: Active Projects
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

    private boolean isUnitManager(UUID userId, UUID orgId) {
        return userOrganizationRepository.findByUserIdAndOrganizationId(userId, orgId)
                .map(uo -> uo.getTitle() != null
                        && (TitleDefault.UNIT_LEADER.getValue().equals(uo.getTitle().getName())
                                || TitleDefault.DEPUTY.getValue().equals(uo.getTitle().getName())))
                .orElse(false);
    }
}
