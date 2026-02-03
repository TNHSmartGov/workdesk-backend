package com.tnh.baseware.core.repositories.stats;

import com.tnh.baseware.core.entities.adu.Organization;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

/**
 * Custom queries for statistics calculation
 * Uses native SQL for optimal performance
 */
@org.springframework.stereotype.Repository
public interface IStatsCalculationRepository extends Repository<Organization, UUID> {

    /**
     * Calculate all task metrics for an organization at a specific snapshot time
     * 
     * @param orgId          Organization ID
     * @param snapshotTime   End time of snapshot period
     * @param snapshotDate   Date of snapshot (for filtering created today)
     * @param threeDaysLater Time point 3 days in future
     * @return Projection with aggregated metrics
     */
    @Query(value = """
            SELECT
                -- Total cumulative (all non-deleted tasks)
                COUNT(DISTINCT t.id) as totalTasks,

                -- New today (created on snapshot date)
                COUNT(DISTINCT CASE
                    WHEN DATE(t.created_date) = CAST(:snapshotDate AS DATE)
                    THEN t.id END) as newTasksToday,

                -- Completed today (status DONE and modified on snapshot date)
                COUNT(DISTINCT CASE
                    WHEN t.status = 'DONE' AND DATE(t.modified_date) = CAST(:snapshotDate AS DATE)
                    THEN t.id END) as completedToday,

                -- Overdue (due_date passed, not DONE/CANCELLED)
                COUNT(DISTINCT CASE
                    WHEN t.due_date < :snapshotTime
                        AND t.status NOT IN ('DONE', 'CANCELLED')
                    THEN t.id END) as overdueTasks,

                -- Due in next 3 days
                COUNT(DISTINCT CASE
                    WHEN t.due_date BETWEEN :snapshotTime AND :threeDaysLater
                        AND t.status NOT IN ('DONE', 'CANCELLED')
                    THEN t.id END) as dueInNext3Days,

                -- In progress
                COUNT(DISTINCT CASE
                    WHEN t.status = 'IN_PROGRESS'
                    THEN t.id END) as inProgressTasks,

                -- Average progress rate (exclude TODO tasks)
                AVG(CASE WHEN t.status != 'TODO' THEN t.progress END) as avgProgressRate

            FROM task t
            INNER JOIN project p ON t.project_id = p.id
            WHERE p.organization_id = :orgId
                AND t.deleted = false
                AND t.created_date <= :snapshotTime
            """, nativeQuery = true)
    TaskStatsProjection calculateTaskStats(
            @Param("orgId") UUID orgId,
            @Param("snapshotTime") Instant snapshotTime,
            @Param("snapshotDate") String snapshotDate,
            @Param("threeDaysLater") Instant threeDaysLater);

    /**
     * Calculate all project metrics for an organization
     */
    @Query(value = """
            SELECT
                -- Total cumulative
                COUNT(DISTINCT p.id) as totalProjects,

                -- Active projects
                COUNT(DISTINCT CASE
                    WHEN p.status = 'ACTIVE'
                    THEN p.id END) as activeProjects,

                -- Overdue projects
                COUNT(DISTINCT CASE
                    WHEN p.end_date < :snapshotTime
                        AND p.status NOT IN ('COMPLETED', 'CANCELLED')
                    THEN p.id END) as overdueProjects,

                -- Completed today
                COUNT(DISTINCT CASE
                    WHEN p.status = 'COMPLETED' AND DATE(p.modified_date) = CAST(:snapshotDate AS DATE)
                    THEN p.id END) as completedProjectsToday

            FROM project p
            WHERE p.organization_id = :orgId
                AND p.deleted = false
                AND p.created_date <= :snapshotTime
            """, nativeQuery = true)
    ProjectStatsProjection calculateProjectStats(
            @Param("orgId") UUID orgId,
            @Param("snapshotTime") Instant snapshotTime,
            @Param("snapshotDate") String snapshotDate);

    /**
     * Count active users (users who created tasks today)
     */
    @Query(value = """
            SELECT COUNT(DISTINCT t.created_by)
            FROM task t
            INNER JOIN project p ON t.project_id = p.id
            WHERE p.organization_id = :orgId
                AND DATE(t.created_date) = CAST(:snapshotDate AS DATE)
                AND t.deleted = false
            """, nativeQuery = true)
    Integer countActiveUsers(
            @Param("orgId") UUID orgId,
            @Param("snapshotDate") String snapshotDate);

    /**
     * Calculate average completion time in hours for tasks completed today
     */
    @Query(value = """
            SELECT AVG(EXTRACT(EPOCH FROM (t.modified_date - t.created_date)) / 3600.0)
            FROM task t
            INNER JOIN project p ON t.project_id = p.id
            WHERE p.organization_id = :orgId
                AND t.status = 'DONE'
                AND DATE(t.modified_date) = CAST(:snapshotDate AS DATE)
                AND t.deleted = false
            """, nativeQuery = true)
    Double calculateAvgCompletionTimeHours(
            @Param("orgId") UUID orgId,
            @Param("snapshotDate") String snapshotDate);
}
