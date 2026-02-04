package com.tnh.baseware.core.repositories.stats;

import com.tnh.baseware.core.entities.stats.OrganizationDailyStats;

import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IOrganizationDailyStatsRepository extends IGenericRepository<OrganizationDailyStats, UUID> {

        /**
         * Find stats by organization and snapshot time
         */
        Optional<OrganizationDailyStats> findByOrganizationIdAndSnapshotTime(
                        UUID organizationId,
                        Instant snapshotTime);

        /**
         * Find stats between times
         */
        List<OrganizationDailyStats> findByOrganizationIdAndSnapshotTimeBetweenOrderBySnapshotTimeDesc(
                        UUID organizationId,
                        Instant startTime,
                        Instant endTime);

        /**
         * Tìm stats cũ cần archive
         */
        List<OrganizationDailyStats> findBySnapshotTimeBeforeAndIsArchived(
                        Instant cutoffTime,
                        Boolean isArchived);

        /**
         * Find stats by list of orgs (for comparison)
         */
        List<OrganizationDailyStats> findByOrganizationIdInAndSnapshotTime(
                        List<UUID> organizationIds,
                        Instant snapshotTime);

        /**
         * Delete stats đã archive quá lâu
         */
        @Modifying
        @Query("DELETE FROM OrganizationDailyStats s WHERE s.isArchived = true AND s.archivedAt < :cutoffTime")
        int deleteArchivedBefore(@Param("cutoffTime") Instant cutoffTime);

        /**
         * Lấy latest snapshot của organization
         */
        @Query("SELECT s FROM OrganizationDailyStats s " +
                        "WHERE s.organization.id = :orgId " +
                        "AND s.isArchived = false " +
                        "ORDER BY s.snapshotTime DESC " +
                        "LIMIT 1")
        Optional<OrganizationDailyStats> findLatestByOrganizationId(@Param("orgId") UUID organizationId);

        @Query("""
                        SELECT new com.tnh.baseware.core.dtos.dashboard.UnitPerformanceDTO(
                            CAST(COALESCE(SUM(s.newTasksToday), 0) AS long),
                            CAST(COALESCE(AVG(s.completionRate), 0.0) AS double),
                            CAST(COALESCE(AVG(s.overdueRate), 0.0) AS double)
                        )
                        FROM OrganizationDailyStats s
                        WHERE s.organization.id = :orgId
                        AND s.snapshotTime >= :from
                        AND s.snapshotTime <= :to
                        """)
        com.tnh.baseware.core.dtos.dashboard.UnitPerformanceDTO aggregatePerformance(
                        @Param("orgId") UUID orgId,
                        @Param("from") Instant from,
                        @Param("to") Instant to);
}
