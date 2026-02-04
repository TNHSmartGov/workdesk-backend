package com.tnh.baseware.core.services.stats.imp;

import com.tnh.baseware.core.dtos.stats.OrganizationDailyStatsDTO;
import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.entities.stats.OrganizationDailyStats;
import com.tnh.baseware.core.enums.stats.SnapshotType;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import com.tnh.baseware.core.mappers.stats.IOrganizationDailyStatsMapper;
import com.tnh.baseware.core.repositories.adu.IOrganizationRepository;
import com.tnh.baseware.core.repositories.stats.*;
import com.tnh.baseware.core.services.stats.IOrganizationStatsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationStatsService implements IOrganizationStatsService {

    IOrganizationDailyStatsRepository statsRepository;
    IStatsCalculationRepository calculationRepository;
    IOrganizationRepository organizationRepository;
    IOrganizationDailyStatsMapper mapper;

    @Override
    @Transactional
    public OrganizationDailyStatsDTO calculateAndSaveStats(
            UUID orgId,
            LocalDate snapshotDate,
            SnapshotType snapshotType) {

        log.debug("Calculating {} stats for organization {} on {}",
                snapshotType, orgId, snapshotDate);

        // Verify organization exists
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new BWCGenericRuntimeException("Organization not found: " + orgId));

        // Define snapshot time based on type
        Instant snapshotTime = calculateSnapshotTime(snapshotDate, snapshotType);
        Instant threeDaysLater = snapshotTime.plus(3, ChronoUnit.DAYS);

        // === Calculate Task Metrics ===
        TaskStatsProjection taskStats = calculationRepository.calculateTaskStats(
                orgId, snapshotTime, snapshotDate.toString(), threeDaysLater);

        // === Calculate Project Metrics ===
        ProjectStatsProjection projectStats = calculationRepository.calculateProjectStats(
                orgId, snapshotTime, snapshotDate.toString());

        // === Calculate Performance Metrics ===
        Integer activeUsers = calculationRepository.countActiveUsers(orgId, snapshotDate.toString());
        Double avgCompletionTime = calculationRepository.calculateAvgCompletionTimeHours(
                orgId, snapshotDate.toString());

        // Calculate derived metrics
        Double completionRate = calculateCompletionRate(
                taskStats.getNewTasksToday(),
                taskStats.getCompletedToday());

        Double overdueRate = calculateOverdueRate(
                taskStats.getTotalTasks(),
                taskStats.getOverdueTasks());

        // === Calculate Extended Metrics (Optional) ===
        Map<String, Object> extendedMetrics = calculateExtendedMetrics(orgId, snapshotDate);

        // Delete existing stats if already exists (for recalculation)
        statsRepository.findByOrganizationIdAndSnapshotDateAndSnapshotType(
                orgId, snapshotDate, snapshotType).ifPresent(statsRepository::delete);

        // Build and save entity
        OrganizationDailyStats entity = OrganizationDailyStats.builder()
                .organization(org)
                .snapshotDate(snapshotDate)
                .snapshotType(snapshotType)
                // Task metrics
                .totalTasks(safeIntValue(taskStats.getTotalTasks()))
                .newTasksToday(safeIntValue(taskStats.getNewTasksToday()))
                .completedToday(safeIntValue(taskStats.getCompletedToday()))
                .overdueTasks(safeIntValue(taskStats.getOverdueTasks()))
                .dueInNext3Days(safeIntValue(taskStats.getDueInNext3Days()))
                .inProgressTasks(safeIntValue(taskStats.getInProgressTasks()))
                .avgProgressRate(taskStats.getAvgProgressRate())
                // Project metrics
                .totalProjects(safeIntValue(projectStats.getTotalProjects()))
                .activeProjects(safeIntValue(projectStats.getActiveProjects()))
                .overdueProjects(safeIntValue(projectStats.getOverdueProjects()))
                .completedProjectsToday(safeIntValue(projectStats.getCompletedProjectsToday()))
                // Performance metrics
                .completionRate(completionRate)
                .overdueRate(overdueRate)
                .activeUserCount(activeUsers != null ? activeUsers : 0)
                .avgCompletionTimeHours(avgCompletionTime)
                // Extended & metadata
                .extendedMetrics(extendedMetrics)
                .calculatedAt(Instant.now())
                .isArchived(false)
                .build();

        OrganizationDailyStats saved = statsRepository.save(entity);

        log.info("Successfully calculated and saved {} stats for org {} on {}: {} total tasks, {} new, {} completed",
                snapshotType, orgId, snapshotDate, saved.getTotalTasks(),
                saved.getNewTasksToday(), saved.getCompletedToday());

        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDailyStatsDTO getStats(UUID orgId, LocalDate date, SnapshotType snapshotType) {
        return statsRepository.findByOrganizationIdAndSnapshotDateAndSnapshotType(orgId, date, snapshotType)
                .map(mapper::entityToDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationDailyStatsDTO> getStatsTrend(UUID orgId, LocalDate startDate, LocalDate endDate) {
        return statsRepository.findByOrganizationIdAndSnapshotDateBetweenOrderBySnapshotDateDescSnapshotTypeAsc(
                orgId, startDate, endDate).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationDailyStatsDTO> compareOrganizations(
            List<UUID> orgIds,
            LocalDate date,
            SnapshotType snapshotType) {
        return statsRepository.findByOrganizationIdInAndSnapshotDateAndSnapshotType(
                orgIds, date, snapshotType).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDailyStatsDTO getLatestStats(UUID orgId) {
        return statsRepository.findLatestByOrganizationId(orgId)
                .map(mapper::entityToDTO)
                .orElse(null);
    }

    @Override
    @Async
    public void triggerRecalculationAsync(UUID orgId, LocalDate date, SnapshotType snapshotType) {
        log.info("Async recalculation triggered for org {} on {} ({})", orgId, date, snapshotType);
        try {
            calculateAndSaveStats(orgId, date, snapshotType);
        } catch (Exception e) {
            log.error("Failed to recalculate stats asynchronously for org {}: {}", orgId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public OrganizationDailyStatsDTO recalculateStats(UUID orgId, LocalDate date, SnapshotType snapshotType) {
        log.info("Recalculating stats for org {} on {} ({})", orgId, date, snapshotType);
        return calculateAndSaveStats(orgId, date, snapshotType);
    }

    // ============ HELPER METHODS ============

    /**
     * Calculate snapshot time based on snapshot type
     */
    private Instant calculateSnapshotTime(LocalDate date, SnapshotType snapshotType) {
        return switch (snapshotType) {
            case MIDDAY -> date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant();
            case END_OF_DAY -> date.atTime(17, 30).atZone(ZoneId.systemDefault()).toInstant();
        };
    }

    /**
     * Calculate completion rate = (completed / new) * 100
     */
    private Double calculateCompletionRate(Long newTasks, Long completedTasks) {
        if (newTasks == null || newTasks == 0)
            return 0.0;
        if (completedTasks == null)
            return 0.0;
        return (completedTasks.doubleValue() / newTasks.doubleValue()) * 100.0;
    }

    /**
     * Calculate overdue rate = (overdue / total) * 100
     */
    private Double calculateOverdueRate(Long totalTasks, Long overdueTasks) {
        if (totalTasks == null || totalTasks == 0)
            return 0.0;
        if (overdueTasks == null)
            return 0.0;
        return (overdueTasks.doubleValue() / totalTasks.doubleValue()) * 100.0;
    }

    /**
     * Calculate extended metrics (flexible JSON field)
     * This can be extended based on business needs
     */
    private Map<String, Object> calculateExtendedMetrics(UUID orgId, LocalDate snapshotDate) {
        Map<String, Object> extended = new HashMap<>();

        // Example: Add more metrics here as needed
        // extended.put("tasksByPriority", calculateTasksByPriority(orgId,
        // snapshotDate));
        // extended.put("tasksByCategory", calculateTasksByCategory(orgId,
        // snapshotDate));

        return extended;
    }

    /**
     * Safe conversion from Long to Integer
     */
    private Integer safeIntValue(Long value) {
        return value != null ? value.intValue() : 0;
    }
}
