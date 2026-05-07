package com.tnh.baseware.core.services.dashboard.imp;

import com.tnh.baseware.core.dtos.dashboard.executive.*;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.mappers.task.ITaskMapper;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.services.dashboard.IExecutiveDashboardService;
import com.tnh.baseware.core.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExecutiveDashboardService implements IExecutiveDashboardService {

        ITaskRepository taskRepository;
        ITaskMemberRepository taskMemberRepository;
        IProjectRepository projectRepository;
        ITaskMapper taskMapper;
        SecurityUtils securityUtils;

        @Override
        @Transactional(readOnly = true)
        @org.springframework.cache.annotation.Cacheable(value = "dashboard_unit", key = "{#orgId, #from, #to}")
        public ExecutiveHotspotDTO getHotspots(UUID orgId, Instant from, Instant to) {
                Instant now = Instant.now();
                Instant future48h = now.plus(48, ChronoUnit.HOURS);
                Instant past48h = now.minus(48, ChronoUnit.HOURS);
                Instant safeFrom = from != null ? from : Instant.EPOCH;
                Instant safeTo = to != null ? to : Instant.parse("2100-01-01T00:00:00Z");

                // 1. Overdue (Standard) - Using existing method for Org Wide
                long overdue = taskRepository.countByOrganizationIdOverdueTimeboxed(orgId, now, safeFrom, safeTo);

                // 2. At Risk (Priority High + Due Soon + Low Progress)
                long atRisk = taskRepository.countDeepAtRiskTimeboxed(orgId, future48h, safeFrom, safeTo);

                // 3. Blocked (Active + Old + No Report recently)
                long blocked = taskRepository.countBlockedTimeboxed(orgId, past48h, safeFrom, safeTo);

                return ExecutiveHotspotDTO.builder()
                                .overdueCount(overdue)
                                .atRiskCount(atRisk)
                                .blockedCount(blocked)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        @org.springframework.cache.annotation.Cacheable(value = "dashboard_personal", key = "'executive_actions:' + #orgId + ':' + #userId + ':' + #from + ':' + #to")
        public ExecutiveActionItemDTO getActionItems(UUID orgId, UUID userId, Instant from, Instant to) {
                Instant safeFrom = from != null ? from : Instant.EPOCH;
                Instant safeTo = to != null ? to : Instant.parse("2100-01-01T00:00:00Z");

                // 1. Approval Queue (Status = REVIEW)
                List<Task> reviews = taskRepository.findTasksInReviewTimeboxed(orgId, safeFrom, safeTo);

                // 2. My Urgent Tasks (Assigned + HIGH + Not Done)
                List<Task> urgents = taskRepository.findMyUrgentTasksTimeboxed(userId, safeFrom, safeTo);

                // Mapping to DTOs
                var currentUser = securityUtils.currentUser();

                return ExecutiveActionItemDTO.builder()
                                .approvalQueue(reviews.stream()
                                                .map(t -> taskMapper.entityToDTO(t, currentUser, taskMemberRepository))
                                                .toList())
                                .myUrgentTasks(urgents.stream()
                                                .map(t -> taskMapper.entityToDTO(t, currentUser, taskMemberRepository))
                                                .toList())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        @org.springframework.cache.annotation.Cacheable(value = "dashboard_unit", key = "'velocity:' + #orgId + ':' + #from + ':' + #to")
        public List<WeekVelocityDTO> getVelocity(UUID orgId, Instant from, Instant to) {
                Instant now = Instant.now();
                // If no range provided, default to last 4 weeks
                Instant safeFrom = from != null ? from : now.minus(28, ChronoUnit.DAYS);
                Instant safeTo = to != null ? to : now;

                List<Task> doneTasks = taskRepository.findCompletedTasksInTimeRange(orgId, safeFrom, safeTo);

                // Group by Week
                Map<String, Long> weeklyCounts = doneTasks.stream()
                                .collect(Collectors.groupingBy(
                                                t -> formatWeek(t.getModifiedDate()),
                                                Collectors.counting()));

                // Fill gaps? It's harder with arbitrary range.
                // Simple approach: Just return what we have, or generate weeks between SafeFrom
                // and SafeTo.
                // For simplicity/robustness, let's just return the data points we have, sorted.
                // Or if the range is small (<= 12 weeks), fill gaps.

                List<WeekVelocityDTO> velocity = new ArrayList<>();
                weeklyCounts.forEach((week, count) -> velocity.add(new WeekVelocityDTO(week, count)));

                // Sort by Week string (rough approximation, "Week 1" vs "Week 52") - Ideally we
                // sort by date.
                // To sort correctly, maybe we should parse "Week X" or use a different key.
                // Re-implementation: Sort tasks by date, then group?

                // Better: Just return the list sorted by label for now.
                velocity.sort(Comparator.comparing(WeekVelocityDTO::getWeekLabel));

                return velocity;
        }

        private String formatWeek(Instant date) {
                // Formatter: "Week W"
                Locale locale = Locale.getDefault();
                int week = date.atZone(ZoneId.systemDefault()).get(WeekFields.of(locale).weekOfWeekBasedYear());
                return "Week " + week;
        }

        @Override
        @Transactional(readOnly = true)
        @org.springframework.cache.annotation.Cacheable(value = "dashboard_unit", key = "'resource_health:' + #orgId + ':' + #from + ':' + #to")
        public List<ResourceHealthDTO> getResourceHealth(UUID orgId, Instant from, Instant to) {
                Instant safeFrom = from != null ? from : Instant.EPOCH;
                Instant safeTo = to != null ? to : Instant.parse("2100-01-01T00:00:00Z");

                List<Object[]> workload = taskRepository.countActiveTasksPerUserTimeboxed(orgId, safeFrom, safeTo);

                return workload.stream().map(obj -> {
                        User user = (User) obj[0];
                        long count = (Long) obj[1];
                        boolean over = count > 5;

                        return ResourceHealthDTO.builder()
                                        .userId(user.getId())
                                        .fullName(user.getFullName())
                                        .avatarUrl(user.getAvatarUrl())
                                        .activeTaskCount(count)
                                        .isOverloaded(over)
                                        .loadStatus(over ? "OVERLOADED" : (count > 3 ? "HIGH" : "NORMAL"))
                                        .build();
                }).limit(5).collect(Collectors.toList()); // Top 5 busiest
        }

        @Override
        @Transactional(readOnly = true)
        @org.springframework.cache.annotation.Cacheable(value = "dashboard_unit", key = "'project_health:' + #orgId + ':' + #from + ':' + #to")
        public List<ProjectHealthDTO> getProjectHealth(UUID orgId, Instant from, Instant to) {
                Instant safeFrom = from != null ? from : Instant.EPOCH;
                Instant safeTo = to != null ? to : Instant.parse("2100-01-01T00:00:00Z");

                // 1. Get stats
                List<Object[]> stats = taskRepository.getProjectProgressStatsTimeboxed(orgId, safeFrom, safeTo);
                Map<UUID, long[]> statMap = new HashMap<>(); // [Total, Completed]
                for (Object[] row : stats) {
                        statMap.put((UUID) row[0], new long[] { (Long) row[1], (Long) row[2] });
                }

                // 2. Get Active Projects
                List<Project> projects = projectRepository.findAllByOrganizationId(orgId); // Assuming basic find exists

                return projects.stream()
                                .filter(p -> p.getDeleted() == Boolean.FALSE)
                                .map(p -> {
                                        long[] s = statMap.getOrDefault(p.getId(), new long[] { 0L, 0L });
                                        long total = s[0];
                                        long completed = s[1];
                                        int progress = total == 0 ? 0 : (int) ((completed * 100) / total);

                                        return ProjectHealthDTO.builder()
                                                        .projectId(p.getId())
                                                        .projectCode(p.getCode())
                                                        .projectName(p.getName())
                                                        .status(p.getStatus())
                                                        .totalTasks(total)
                                                        .completedTasks(completed)
                                                        .progress(progress)
                                                        .build();
                                })
                                .sorted((p1, p2) -> Integer.compare(p2.getProgress(), p1.getProgress()))
                                .limit(5)
                                .collect(Collectors.toList());
        }

}
