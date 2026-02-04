package com.tnh.baseware.core.dtos.stats;

import com.tnh.baseware.core.dtos.basic.BasicOrganizationDTO;
import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.enums.stats.SnapshotType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.RepresentationModel;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * DTO cho Organization Daily Stats
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationDailyStatsDTO extends RepresentationModel<OrganizationDailyStatsDTO>
        implements Identifiable<UUID> {

    UUID id;
    BasicOrganizationDTO organization;
    LocalDate snapshotDate;
    SnapshotType snapshotType;

    // Grouped metrics for better frontend consumption
    TaskMetricsDTO taskMetrics;
    ProjectMetricsDTO projectMetrics;
    PerformanceMetricsDTO performanceMetrics;

    // Flexible extended metrics
    Map<String, Object> extendedMetrics;

    Instant calculatedAt;
    Boolean isArchived;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TaskMetricsDTO {
        Integer total;
        Integer newToday;
        Integer completedToday;
        Integer overdue;
        Integer dueInNext3Days;
        Integer inProgress;
        Double avgProgress;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProjectMetricsDTO {
        Integer total;
        Integer active;
        Integer overdue;
        Integer completedToday;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PerformanceMetricsDTO {
        Double completionRate;
        Double overdueRate;
        Integer activeUserCount;
        Double avgCompletionTimeHours;
    }
}
