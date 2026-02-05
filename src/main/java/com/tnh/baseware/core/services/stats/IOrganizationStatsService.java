package com.tnh.baseware.core.services.stats;

import com.tnh.baseware.core.dtos.stats.OrganizationDailyStatsDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Organization Daily Statistics
 */
public interface IOrganizationStatsService {

        /**
         * Calculate and save stats for an organization on a specific date
         *
         * @param orgId        Organization ID
         * @param snapshotDate Date to calculate stats for
         * @return Saved stats DTO
         */
        OrganizationDailyStatsDTO calculateAndSaveStats(
                        UUID orgId,
                        Instant snapshotDate); // Changed from LocalDate

        /**
         * Get stats for a specific date
         */
        OrganizationDailyStatsDTO getStats(
                        UUID orgId,
                        Instant date); // Changed from LocalDate

        /**
         * Get trend over a period
         */
        List<OrganizationDailyStatsDTO> getStatsTrend(
                        UUID orgId,
                        Instant startDate, // Changed from LocalDate
                        Instant endDate); // Changed from LocalDate

        /**
         * Compare organizations on a specific date
         */
        List<OrganizationDailyStatsDTO> compareOrganizations(
                        List<UUID> orgIds,
                        Instant date); // Changed from LocalDate

        /**
         * Get latest available stats
         */
        OrganizationDailyStatsDTO getLatestStats(UUID orgId);

        /**
         * Recalculate synchronously
         */
        OrganizationDailyStatsDTO recalculateStats(
                        UUID orgId,
                        Instant date); // Changed from LocalDate

        /**
         * Trigger recalculation asynchronously
         */
        void triggerRecalculationAsync(
                        UUID orgId,
                        Instant date);
}
