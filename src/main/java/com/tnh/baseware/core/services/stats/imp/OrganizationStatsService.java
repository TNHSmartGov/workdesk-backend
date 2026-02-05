package com.tnh.baseware.core.services.stats.imp;

import com.tnh.baseware.core.dtos.stats.OrganizationDailyStatsDTO;
import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.entities.stats.OrganizationDailyStats;

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
            Instant inputTime) {

        // Normalize to Start of Day (UTC) to ensure uniqueness
        Instant snapshotTime = normalizeToStartOfDay(inputTime);
        log.debug("Calculating stats for organization {} on {} (normalized)", orgId, snapshotTime);

        // Verify organization exists
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new BWCGenericRuntimeException("Organization not found: " + orgId));

        Instant threeDaysLater = snapshotTime.plus(3, ChronoUnit.DAYS);

        String dateString = snapshotTime.atZone(ZoneId.of("UTC")).toLocalDate().toString();

        // === Calculate Task Metrics ===
        TaskStatsProjection taskStats = calculationRepository.calculateTaskStats(
                orgId, snapshotTime, dateString, threeDaysLater);

        // === Calculate Project Metrics ===
        ProjectStatsProjection projectStats = calculationRepository.calculateProjectStats(
                orgId, snapshotTime, dateString);

        // === Calculate Performance Metrics ===
        Integer activeUsers = calculationRepository.countActiveUsers(orgId, dateString);
        Double avgCompletionTime = calculationRepository.calculateAvgCompletionTimeHours(
                orgId, dateString);

        // Calculate derived metrics
        Double completionRate = calculateCompletionRate(
                taskStats.getNewTasksToday(),
                taskStats.getCompletedToday());

        Double overdueRate = calculateOverdueRate(
                taskStats.getTotalTasks(),
                taskStats.getOverdueTasks());

        // === Calculate Extended Metrics (Optional) ===
        Map<String, Object> extendedMetrics = calculateExtendedMetrics(orgId, snapshotTime);

        // Check if stats already exist for this day (normalized time)
        OrganizationDailyStats entity = statsRepository.findByOrganizationIdAndSnapshotTime(orgId, snapshotTime)
                .orElse(OrganizationDailyStats.builder()
                        .organization(org)
                        .snapshotTime(snapshotTime)
                        .build());

        // Update fields
        entity.setTotalTasks(safeIntValue(taskStats.getTotalTasks()));
        entity.setNewTasksToday(safeIntValue(taskStats.getNewTasksToday()));
        entity.setCompletedToday(safeIntValue(taskStats.getCompletedToday()));
        entity.setOverdueTasks(safeIntValue(taskStats.getOverdueTasks()));
        entity.setDueInNext3Days(safeIntValue(taskStats.getDueInNext3Days()));
        entity.setInProgressTasks(safeIntValue(taskStats.getInProgressTasks()));
        entity.setAvgProgressRate(taskStats.getAvgProgressRate());

        entity.setTotalProjects(safeIntValue(projectStats.getTotalProjects()));
        entity.setActiveProjects(safeIntValue(projectStats.getActiveProjects()));
        entity.setOverdueProjects(safeIntValue(projectStats.getOverdueProjects()));
        entity.setCompletedProjectsToday(safeIntValue(projectStats.getCompletedProjectsToday()));

        entity.setCompletionRate(completionRate);
        entity.setOverdueRate(overdueRate);
        entity.setActiveUserCount(activeUsers != null ? activeUsers : 0);
        entity.setAvgCompletionTimeHours(avgCompletionTime);

        entity.setExtendedMetrics(extendedMetrics);
        entity.setCalculatedAt(Instant.now());
        // entity.setIsArchived(false); // Default

        OrganizationDailyStats saved = statsRepository.save(entity);

        log.info("Successfully calculated and saved stats for org {} on {}: {} total tasks, {} new, {} completed",
                orgId, snapshotTime, saved.getTotalTasks(),
                saved.getNewTasksToday(), saved.getCompletedToday());

        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDailyStatsDTO getStats(UUID orgId, Instant date) {
        Instant normalizedDate = normalizeToStartOfDay(date);
        return statsRepository.findByOrganizationIdAndSnapshotTime(orgId, normalizedDate)
                .map(mapper::entityToDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationDailyStatsDTO> getStatsTrend(UUID orgId, Instant from, Instant to) {
        Instant normalizedFrom = normalizeToStartOfDay(from);
        Instant normalizedTo = normalizeToStartOfDay(to);

        return statsRepository.findByOrganizationIdAndSnapshotTimeBetweenOrderBySnapshotTimeDesc(
                orgId, normalizedFrom, normalizedTo).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationDailyStatsDTO> compareOrganizations(
            List<UUID> orgIds,
            Instant date) {
        Instant normalizedDate = normalizeToStartOfDay(date);
        return statsRepository.findByOrganizationIdInAndSnapshotTime(
                orgIds, normalizedDate).stream()
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
    public void triggerRecalculationAsync(UUID orgId, Instant date) {
        log.info("Async recalculation triggered for org {} on {}", orgId, date);
        try {
            calculateAndSaveStats(orgId, date);
        } catch (Exception e) {
            log.error("Failed to recalculate stats asynchronously for org {}: {}", orgId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public OrganizationDailyStatsDTO recalculateStats(UUID orgId, Instant date) {
        log.info("Recalculating stats for org {} on {}", orgId, date);
        return calculateAndSaveStats(orgId, date);
    }

    // ============ HELPER METHODS ============

    /**
     * Normalize Instant to Start of Day (UTC)
     */
    private Instant normalizeToStartOfDay(Instant instant) {
        if (instant == null)
            return null;
        return instant.atZone(ZoneId.of("UTC"))
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant();
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
    private Map<String, Object> calculateExtendedMetrics(UUID orgId, Instant snapshotTime) {
        Map<String, Object> extended = new HashMap<>();
        return extended;
    }

    /**
     * Safe conversion from Long to Integer
     */
    private Integer safeIntValue(Long value) {
        return value != null ? value.intValue() : 0;
    }
}
