package com.tnh.baseware.core.repositories.stats;

import com.tnh.baseware.core.entities.stats.OrganizationDailyStats;
import com.tnh.baseware.core.enums.stats.SnapshotType;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IOrganizationDailyStatsRepository extends IGenericRepository<OrganizationDailyStats, UUID> {

    /**
     * Tìm stats của 1 organization trong 1 ngày cụ thể với snapshot type
     */
    Optional<OrganizationDailyStats> findByOrganizationIdAndSnapshotDateAndSnapshotType(
            UUID organizationId,
            LocalDate snapshotDate,
            SnapshotType snapshotType);

    /**
     * Lấy stats của 1 organization trong khoảng thời gian
     */
    List<OrganizationDailyStats> findByOrganizationIdAndSnapshotDateBetweenOrderBySnapshotDateDescSnapshotTypeAsc(
            UUID organizationId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * Lấy stats của nhiều organizations trong 1 ngày
     */
    List<OrganizationDailyStats> findByOrganizationIdInAndSnapshotDateAndSnapshotType(
            List<UUID> organizationIds,
            LocalDate snapshotDate,
            SnapshotType snapshotType);

    /**
     * Tìm stats cũ cần archive
     */
    List<OrganizationDailyStats> findBySnapshotDateBeforeAndIsArchived(
            LocalDate cutoffDate,
            Boolean isArchived);

    /**
     * Delete stats đã archive quá lâu
     */
    @Modifying
    @Query("DELETE FROM OrganizationDailyStats s WHERE s.isArchived = true AND s.archivedAt < :cutoffTime")
    int deleteArchivedBefore(@Param("cutoffTime") Instant cutoffTime);

    /**
     * Kiểm tra snapshot đã tồn tại chưa
     */
    boolean existsByOrganizationIdAndSnapshotDateAndSnapshotType(
            UUID organizationId,
            LocalDate snapshotDate,
            SnapshotType snapshotType);

    /**
     * Lấy latest snapshot của organization
     */
    @Query("SELECT s FROM OrganizationDailyStats s " +
            "WHERE s.organization.id = :orgId " +
            "AND s.isArchived = false " +
            "ORDER BY s.snapshotDate DESC, s.snapshotType DESC " +
            "LIMIT 1")
    Optional<OrganizationDailyStats> findLatestByOrganizationId(@Param("orgId") UUID organizationId);
}
