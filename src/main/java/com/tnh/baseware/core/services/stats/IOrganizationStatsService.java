package com.tnh.baseware.core.services.stats;

import com.tnh.baseware.core.dtos.stats.OrganizationDailyStatsDTO;
import com.tnh.baseware.core.enums.stats.SnapshotType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Organization Daily Statistics
 */
public interface IOrganizationStatsService {

        /**
         * Calculate and save daily stats for an organization
         * 
         * @param orgId        Organization ID
         * @param snapshotDate Date to calculate stats for
         * @param snapshotType MIDDAY or END_OF_DAY
         * @return Saved stats DTO
         */
        OrganizationDailyStatsDTO calculateAndSaveStats(
                        UUID orgId,
                        LocalDate snapshotDate,
                        SnapshotType snapshotType);

        /**
         * Get stats for a specific date and snapshot type
         */
        OrganizationDailyStatsDTO getStats(
                        UUID orgId,
                        LocalDate date,
                        SnapshotType snapshotType);

        /**
         * Get stats trend over a date range
         */
        List<OrganizationDailyStatsDTO> getStatsTrend(
                        UUID orgId,
                        LocalDate startDate,
                        LocalDate endDate);

        /**
         * Compare multiple organizations on a specific date
         */
        List<OrganizationDailyStatsDTO> compareOrganizations(
                        List<UUID> orgIds,
                        LocalDate date,
                        SnapshotType snapshotType);

        /**
         * Get latest stats for an organization
         */
        OrganizationDailyStatsDTO getLatestStats(UUID orgId);

        /**
         * Recalculate stats (for backfill or manual trigger)
         */
        OrganizationDailyStatsDTO recalculateStats(
                        UUID orgId,
                        LocalDate date,
                        SnapshotType snapshotType);

        /**
         * Trigger asynchronous recalculation of stats
         * Wraps execution in @Async context
         */
        void triggerRecalculationAsync(
                        UUID orgId,
                        LocalDate date,
                        SnapshotType snapshotType);
}
