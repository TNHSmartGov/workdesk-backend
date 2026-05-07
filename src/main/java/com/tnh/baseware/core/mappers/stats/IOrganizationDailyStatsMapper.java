package com.tnh.baseware.core.mappers.stats;

import com.tnh.baseware.core.dtos.basic.BasicOrganizationDTO;
import com.tnh.baseware.core.dtos.stats.OrganizationDailyStatsDTO;
import com.tnh.baseware.core.entities.stats.OrganizationDailyStats;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IOrganizationDailyStatsMapper {

    @Mappings({
            @Mapping(target = "organization", source = "organization"),
            @Mapping(target = "taskMetrics", expression = "java(mapTaskMetrics(entity))"),
            @Mapping(target = "projectMetrics", expression = "java(mapProjectMetrics(entity))"),
            @Mapping(target = "performanceMetrics", expression = "java(mapPerformanceMetrics(entity))")
    })
    OrganizationDailyStatsDTO entityToDTO(OrganizationDailyStats entity);

    default BasicOrganizationDTO mapOrganization(com.tnh.baseware.core.entities.adu.Organization org) {
        if (org == null)
            return null;
        return BasicOrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .code(org.getCode())
                .build();
    }

    default OrganizationDailyStatsDTO.TaskMetricsDTO mapTaskMetrics(OrganizationDailyStats entity) {
        return OrganizationDailyStatsDTO.TaskMetricsDTO.builder()
                .total(entity.getTotalTasks())
                .newToday(entity.getNewTasksToday())
                .completedToday(entity.getCompletedToday())
                .overdue(entity.getOverdueTasks())
                .dueInNext3Days(entity.getDueInNext3Days())
                .inProgress(entity.getInProgressTasks())
                .avgProgress(entity.getAvgProgressRate())
                .build();
    }

    default OrganizationDailyStatsDTO.ProjectMetricsDTO mapProjectMetrics(OrganizationDailyStats entity) {
        return OrganizationDailyStatsDTO.ProjectMetricsDTO.builder()
                .total(entity.getTotalProjects())
                .active(entity.getActiveProjects())
                .overdue(entity.getOverdueProjects())
                .completedToday(entity.getCompletedProjectsToday())
                .build();
    }

    default OrganizationDailyStatsDTO.PerformanceMetricsDTO mapPerformanceMetrics(OrganizationDailyStats entity) {
        return OrganizationDailyStatsDTO.PerformanceMetricsDTO.builder()
                .completionRate(entity.getCompletionRate())
                .overdueRate(entity.getOverdueRate())
                .activeUserCount(entity.getActiveUserCount())
                .avgCompletionTimeHours(entity.getAvgCompletionTimeHours())
                .build();
    }
}
