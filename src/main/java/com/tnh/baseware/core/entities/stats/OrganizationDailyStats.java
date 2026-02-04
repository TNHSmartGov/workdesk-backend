package com.tnh.baseware.core.entities.stats;

import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.enums.stats.SnapshotType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Entity lưu trữ thống kê hằng ngày cho từng Organization
 * 
 * Mỗi organization sẽ có 2 snapshot/ngày:
 * - MIDDAY (12:00 PM): Thống kê buổi sáng
 * - END_OF_DAY (17:30 PM): Thống kê cả ngày
 * 
 * Snapshot chỉ chạy vào các ngày làm việc (T2-T6)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "organization_daily_stats", uniqueConstraints = @UniqueConstraint(name = "uk_org_date_type", columnNames = {
        "organization_id", "snapshot_date", "snapshot_type" }))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationDailyStats extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    Organization organization;

    @Column(nullable = false, name = "snapshot_date")
    LocalDate snapshotDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "snapshot_type", length = 20)
    SnapshotType snapshotType;

    // ============ TASK METRICS ============

    @Column(name = "total_tasks")
    Integer totalTasks; // Tổng task lũy kế (không bao gồm deleted)

    @Column(name = "new_tasks_today")
    Integer newTasksToday; // Task phát sinh trong ngày

    @Column(name = "completed_today")
    Integer completedToday; // Task hoàn thành trong ngày

    @Column(name = "overdue_tasks")
    Integer overdueTasks; // Task đã quá hạn

    @Column(name = "due_in_next_3_days")
    Integer dueInNext3Days; // Task sắp đến hạn (3 ngày)

    @Column(name = "in_progress_tasks")
    Integer inProgressTasks; // Task đang thực hiện

    @Column(name = "avg_progress_rate")
    Double avgProgressRate; // % hoàn thành trung bình

    // ============ PROJECT METRICS ============

    @Column(name = "total_projects")
    Integer totalProjects; // Tổng dự án lũy kế

    @Column(name = "active_projects")
    Integer activeProjects; // Dự án đang chạy (status = ACTIVE)

    @Column(name = "overdue_projects")
    Integer overdueProjects; // Dự án trễ hạn

    @Column(name = "completed_projects_today")
    Integer completedProjectsToday; // Dự án hoàn thành hôm nay

    // ============ PERFORMANCE METRICS ============

    @Column(name = "completion_rate")
    Double completionRate; // Tỷ lệ giải quyết công việc (%)

    @Column(name = "overdue_rate")
    Double overdueRate; // Tỷ lệ trễ hạn (%)

    @Column(name = "active_user_count")
    Integer activeUserCount; // Số người tham gia làm việc trong ngày

    @Column(name = "avg_completion_time_hours")
    Double avgCompletionTimeHours; // Thời gian xử lý trung bình (giờ)

    // ============ FLEXIBLE EXTENDED METRICS ============

    /**
     * Metrics tùy chỉnh linh hoạt lưu dưới dạng JSON
     * Ví dụ: tasksByPriority, tasksByCategory, sprintMetrics, customKPIs
     */
    @Column(name = "extended_metrics", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> extendedMetrics;

    // ============ METADATA ============

    @Column(name = "calculated_at")
    Instant calculatedAt; // Thời điểm thực hiện tính toán

    @Builder.Default
    @Column(name = "is_archived")
    Boolean isArchived = false; // Đánh dấu đã archive (data > 2 năm)

    @Column(name = "archived_at")
    Instant archivedAt; // Thời điểm archive
}
