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
        @org.springframework.cache.annotation.Cacheable(value = "dashboard_unit", key = "#orgId")
        public ExecutiveHotspotDTO getHotspots(UUID orgId) {
                Instant now = Instant.now();
                Instant future48h = now.plus(48, ChronoUnit.HOURS);
                Instant past48h = now.minus(48, ChronoUnit.HOURS);

                // 1. Overdue (Standard) - Using existing method for Org Wide
                long overdue = taskRepository.countByOrganizationIdOverdue(orgId, now);

                // 2. At Risk (Priority High + Due Soon + Low Progress)
                long atRisk = taskRepository.countDeepAtRisk(orgId, future48h);

                // 3. Blocked (Active + Old + No Report recently)
                long blocked = taskRepository.countBlocked(orgId, past48h);

                return ExecutiveHotspotDTO.builder()
                                .overdueCount(overdue)
                                .atRiskCount(atRisk)
                                .blockedCount(blocked)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        @org.springframework.cache.annotation.Cacheable(value = "dashboard_personal", key = "'executive_actions:' + #orgId + ':' + #userId")
        public ExecutiveActionItemDTO getActionItems(UUID orgId, UUID userId) {
                // 1. Approval Queue (Status = REVIEW)
                // Ideally this should filter by permissions, but for "Unit Leader" view,
                // seeing all REVIEW tasks in the unit is appropriate.
                List<Task> reviews = taskRepository.findTasksInReview(orgId);

                // 2. My Urgent Tasks (Assigned + HIGH + Not Done)
                List<Task> urgents = taskRepository.findMyUrgentTasks(userId);

                // Mapping to DTOs
                var currentUser = securityUtils.currentUser(); // This might be cached or efficient enough. If needed we
                                                               // can pass User too, but ID is enough for cache key.

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
        @org.springframework.cache.annotation.Cacheable(value = "dashboard_unit", key = "'velocity:' + #orgId")
        public List<WeekVelocityDTO> getVelocity(UUID orgId) {
                Instant now = Instant.now();
                Instant start = now.minus(28, ChronoUnit.DAYS); // Last 4 weeks

                List<Task> doneTasks = taskRepository.findCompletedTasksInRange(orgId, start);

                // Group by Week
                Map<String, Long> weeklyCounts = doneTasks.stream()
                                .collect(Collectors.groupingBy(
                                                t -> formatWeek(t.getModifiedDate()),
                                                Collectors.counting()));

                // Fill gaps and sort
                List<WeekVelocityDTO> velocity = new ArrayList<>();
                // Logic to generate last 4 week labels ensures we have 4 entries even if 0
                for (int i = 0; i < 4; i++) {
                        Instant weekInst = now.minus(i * 7L, ChronoUnit.DAYS);
                        String weekLabel = formatWeek(weekInst);
                        velocity.add(new WeekVelocityDTO(weekLabel, weeklyCounts.getOrDefault(weekLabel, 0L)));
                }
                Collections.reverse(velocity); // Oldest to Newest
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
        @org.springframework.cache.annotation.Cacheable(value = "dashboard_unit", key = "'resource_health:' + #orgId")
        public List<ResourceHealthDTO> getResourceHealth(UUID orgId) {
                List<Object[]> workload = taskRepository.countActiveTasksPerUser(orgId);

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
        @org.springframework.cache.annotation.Cacheable(value = "dashboard_unit", key = "'project_health:' + #orgId")
        public List<ProjectHealthDTO> getProjectHealth(UUID orgId) {
                // 1. Get stats
                List<Object[]> stats = taskRepository.getProjectProgressStats(orgId);
                Map<UUID, long[]> statMap = new HashMap<>(); // [Total, Completed]
                for (Object[] row : stats) {
                        statMap.put((UUID) row[0], new long[] { (Long) row[1], (Long) row[2] });
                }

                // 2. Get Active Projects
                List<Project> projects = projectRepository.findAllByOrganizationId(orgId); // Assuming basic find exists

                return projects.stream()
                                .filter(p -> p.getDeleted() == Boolean.FALSE) // Double check soft delete in memory or
                                                                              // repo
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
                                .sorted((p1, p2) -> Integer.compare(p2.getProgress(), p1.getProgress())) // Sort by
                                                                                                         // Progress
                                                                                                         // desc? Or
                                                                                                         // recency?
                                                                                                         // Let's do
                                                                                                         // Progress for
                                                                                                         // now.
                                .limit(5)
                                .collect(Collectors.toList());
        }

}
