package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.dtos.task.UserTaskPermissionDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskRepository extends IGenericRepository<Task, UUID> {
  @Query("""
           SELECT
               pm.role as projectRole,
               tm.role as taskRole
           FROM Task t
           LEFT JOIN ProjectMember pm ON pm.project.id = t.project.id AND pm.user.id = :userId
           LEFT JOIN TaskMember tm ON tm.task.id = t.id AND tm.user.id = :userId
           WHERE t.id = :taskId AND t.deleted = false
      """)
  Optional<UserTaskPermissionDTO> findUserPermissions(@Param("taskId") UUID taskId,
      @Param("userId") UUID userId);

  @Query("SELECT t FROM Task t LEFT JOIN t.project p LEFT JOIN p.organization o WHERE (p IS NULL OR o.id = :orgId) AND t.deleted = false")
  List<Task> findByOrganizationId(@Param("orgId") UUID orgId);

  @Query("SELECT t FROM Task t LEFT JOIN t.project p LEFT JOIN p.organization o WHERE (p IS NULL OR o.id = :orgId) AND t.deleted = false")
  Page<Task> findByOrganizationId(@Param("orgId") UUID orgId, Pageable pageable);

  @Query("""
      SELECT DISTINCT t
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  List<Task> findAccessibleByUser(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId);

  @Query("""
      SELECT DISTINCT t FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
      AND t.deleted = false
      AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  Page<Task> findAccessibleByUser(@Param("orgId") UUID orgId, @Param("userId") UUID userId, Pageable pageable);

  @Query("SELECT t FROM Task t LEFT JOIN t.project p LEFT JOIN p.organization o WHERE t.id = :taskId AND (p IS NULL OR o.id = :orgId) AND t.deleted = false")
  Optional<Task> findByIdAndOrganizationId(@Param("taskId") UUID taskId, @Param("orgId") UUID orgId);

  @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.project.organization.id = :orgId AND t.deleted = false ORDER BY t.createdDate DESC")
  List<Task> findByProjectIdAndOrgId(@Param("projectId") UUID projectId, @Param("orgId") UUID orgId);

  @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.project.organization.id = :orgId AND t.deleted = false ORDER BY t.createdDate DESC")
  Page<Task> findByProjectIdAndOrgId(@Param("projectId") UUID projectId, @Param("orgId") UUID orgId,
      Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.taskList.id = :taskListId AND t.project.organization.id = :orgId AND t.deleted = false ORDER BY t.createdDate DESC")
  List<Task> findByTaskListIdAndOrgId(@Param("taskListId") UUID taskListId, @Param("orgId") UUID orgId);

  @Query("SELECT t FROM Task t WHERE t.taskList.id = :taskListId AND t.project.organization.id = :orgId AND t.deleted = false ORDER BY t.createdDate DESC")
  Page<Task> findByTaskListIdAndOrgId(@Param("taskListId") UUID taskListId, @Param("orgId") UUID orgId,
      Pageable pageable);

  @Query("SELECT t FROM Task t LEFT JOIN t.project p LEFT JOIN p.organization o WHERE t.status = :status AND (p IS NULL OR o.id = :orgId) AND t.deleted = false")
  List<Task> findByStatusAndOrgId(@Param("status") TaskStatus status, @Param("orgId") UUID orgId);

  @Query("SELECT t FROM Task t LEFT JOIN t.project p LEFT JOIN p.organization o WHERE t.status = :status AND (p IS NULL OR o.id = :orgId) AND t.deleted = false")
  Page<Task> findByStatusAndOrgId(@Param("status") TaskStatus status, @Param("orgId") UUID orgId,
      Pageable pageable);

  // STATISTICS QUERIES
  @Query("""
      SELECT COUNT( DISTINCT t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  long countAccessibleByUser(@Param("orgId") UUID orgId, @Param("userId") UUID userId);

  @Query("""
      SELECT COUNT( DISTINCT t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.status = :status
        AND t.deleted = false
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  long countAccessibleByStatus(@Param("orgId") UUID orgId, @Param("userId") UUID userId,
      @Param("status") TaskStatus status);

  @Query("""
      SELECT COUNT( DISTINCT t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.dueDate < :now
        AND t.deleted = false
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  long countAccessibleOverdue(@Param("orgId") UUID orgId, @Param("userId") UUID userId,
      @Param("now") java.time.Instant now);

  @Query("""
      SELECT COUNT( DISTINCT t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.dueDate >= :now AND t.dueDate <= :future
        AND t.deleted = false
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  long countAccessibleDueSoon(@Param("orgId") UUID orgId, @Param("userId") UUID userId,
      @Param("now") java.time.Instant now, @Param("future") java.time.Instant future);

  @Query("""
      SELECT DISTINCT t
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.dueDate >= :start AND t.dueDate <= :end
        AND t.deleted = false
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  List<Task> findAccessibleByDueDateRange(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId,
      @Param("start") java.time.Instant start,
      @Param("end") java.time.Instant end);

  // TIMEBOXED COUNTS (For Date Range Filtering)

  // TIMEBOXED COUNTS (For Date Range Filtering)

  // TIMEBOXED COUNTS (For Date Range Filtering)

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.createdDate >= :from
        AND t.createdDate <= :to
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  long countAccessibleByUserTimeboxed(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.status = :status
        AND t.createdDate >= :from
        AND t.createdDate <= :to
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  long countAccessibleByStatusTimeboxed(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId,
      @Param("status") TaskStatus status,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.status = :status
        AND t.modifiedDate >= :from
        AND t.modifiedDate <= :to
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  long countAccessibleByStatusFinishedTimeboxed(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId,
      @Param("status") TaskStatus status,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);

  @Query("""
      SELECT COUNT( DISTINCT t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.dueDate < :now
        AND t.createdDate >= :from
        AND t.createdDate <= :to
        AND t.deleted = false
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  long countAccessibleOverdueTimeboxed(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId,
      @Param("now") java.time.Instant now,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);

  @Query("""
      SELECT COUNT( DISTINCT t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.dueDate >= :now AND t.dueDate <= :future
        AND t.createdDate >= :from
        AND t.createdDate <= :to
        AND t.deleted = false
        AND EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
      """)
  long countAccessibleDueSoonTimeboxed(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId,
      @Param("now") java.time.Instant now,
      @Param("future") java.time.Instant future,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);

  // ORG WIDE TIMEBOXED

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.createdDate >= :from
        AND t.createdDate <= :to
      """)
  long countByOrganizationIdTimeboxed(
      @Param("orgId") UUID orgId,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.status = :status
        AND t.createdDate >= :from
        AND t.createdDate <= :to
      """)
  long countByOrganizationIdAndStatusTimeboxed(
      @Param("orgId") UUID orgId,
      @Param("status") TaskStatus status,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.status = :status
        AND t.modifiedDate >= :from
        AND t.modifiedDate <= :to
      """)
  long countByOrganizationIdAndStatusFinishedTimeboxed(
      @Param("orgId") UUID orgId,
      @Param("status") TaskStatus status,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.dueDate < :now
        AND t.createdDate >= :from
        AND t.createdDate <= :to
        AND t.deleted = false
      """)
  long countByOrganizationIdOverdueTimeboxed(
      @Param("orgId") UUID orgId,
      @Param("now") java.time.Instant now,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.dueDate >= :now AND t.dueDate <= :future
        AND t.createdDate >= :from
        AND t.createdDate <= :to
        AND t.deleted = false
      """)
  long countByOrganizationIdDueSoonTimeboxed(
      @Param("orgId") UUID orgId,
      @Param("now") java.time.Instant now,
      @Param("future") java.time.Instant future,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);

  // GLOBAL STATISTICS QUERIES (FOR UNIT LEADER/DEPUTY)
  @Query("SELECT COUNT(t) FROM Task t LEFT JOIN t.project p LEFT JOIN p.organization o WHERE (p IS NULL OR o.id = :orgId) AND t.deleted = false")
  long countByOrganizationId(@Param("orgId") UUID orgId);

  @Query("SELECT COUNT(t) FROM Task t LEFT JOIN t.project p LEFT JOIN p.organization o WHERE (p IS NULL OR o.id = :orgId) AND t.status = :status AND t.deleted = false")
  long countByOrganizationIdAndStatus(@Param("orgId") UUID orgId, @Param("status") TaskStatus status);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.dueDate < :now
        AND t.deleted = false
      """)
  long countByOrganizationIdOverdue(@Param("orgId") UUID orgId, @Param("now") java.time.Instant now);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.dueDate >= :now AND t.dueDate <= :future
        AND t.deleted = false
      """)
  long countByOrganizationIdDueSoon(@Param("orgId") UUID orgId,
      @Param("now") java.time.Instant now, @Param("future") java.time.Instant future);

  // EXECUTIVE DASHBOARD QUERIES

  // 1. AT RISK: High Priority AND Due < 48h AND Progress < 20%
  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.priority = 'HIGH'
        AND t.dueDate <= :future
        AND t.progress < 20
      """)
  long countDeepAtRisk(
      @Param("orgId") UUID orgId,
      @Param("future") java.time.Instant future);

  // 2. BLOCKED: Active AND Created > 48h AND No Report in last 48h
  @Query("""
      SELECT COUNT(t)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.createdDate < :past
        AND NOT EXISTS (
            SELECT 1 FROM TaskComment c
            WHERE c.task.id = t.id
            AND c.type = 'REPORT'
            AND c.createdDate > :past
        )
      """)
  long countBlocked(
      @Param("orgId") UUID orgId,
      @Param("past") java.time.Instant past);

  // 3. URGENT TASKS FOR ME: Assigned to me AND High Priority AND Not Done
  @Query("""
      SELECT DISTINCT t
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      JOIN TaskMember tm ON tm.task.id = t.id
      WHERE tm.user.id = :userId
        AND t.deleted = false
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
        AND t.priority = 'HIGH'
      ORDER BY t.dueDate ASC
      """)
  List<Task> findMyUrgentTasks(@Param("userId") UUID userId);

  // 4. APPROVAL QUEUE: Status = REVIEW AND (I am reviewer OR I am Unit Manager)
  // Note: Since 'reviewer' concept isn't strictly defined in Task entity yet,
  // we assume for now that Unit Leaders see all tasks in REVIEW state for their
  // Org,
  // or tasks where they are explicitly assigned.
  // Making this broader: All tasks in REVIEW in accessible scope.
  // 4. APPROVAL QUEUE: Status = REVIEW
  @Query("""
      SELECT DISTINCT t
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.status = 'REVIEW'
      """)
  List<Task> findTasksInReview(@Param("orgId") UUID orgId);

  // 5. VELOCITY DATA: Completed tasks in range
  @Query("""
      SELECT t
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.status = 'DONE'
        AND t.modifiedDate >= :startDate
      """)
  List<Task> findCompletedTasksInRange(
      @Param("orgId") UUID orgId,
      @Param("startDate") java.time.Instant startDate);

  // 6. RESOURCE WORKLOAD AGGREGATION
  // Returns Object array: [User, Long count]
  @Query("""
      SELECT tm.user, COUNT(distinct t.id)
      FROM Task t
      JOIN TaskMember tm ON tm.task.id = t.id
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE (p IS NULL OR o.id = :orgId)
        AND t.deleted = false
        AND t.status != 'DONE' AND t.status != 'CANCELLED'
      GROUP BY tm.user
      ORDER BY COUNT(distinct t.id) DESC
      """)
  List<Object[]> countActiveTasksPerUser(@Param("orgId") UUID orgId);

  // 7. PROJECT PROGRESS STATS
  // Returns Object array: [ProjectId, TotalCount, CompletedCount]
  @Query("""
      SELECT t.project.id,
             COUNT(t.id),
             SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END)
      FROM Task t
      LEFT JOIN t.project p
      LEFT JOIN p.organization o
      WHERE o.id = :orgId
        AND t.project IS NOT NULL
        AND t.deleted = false
      GROUP BY t.project.id
      """)
  List<Object[]> getProjectProgressStats(@Param("orgId") UUID orgId);
}
