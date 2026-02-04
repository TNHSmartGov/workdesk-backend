package com.tnh.baseware.core.repositories.stats;

/**
 * Projection interface for task statistics calculation
 * Used in native query aggregation
 */
public interface TaskStatsProjection {
    Long getTotalTasks();

    Long getNewTasksToday();

    Long getCompletedToday();

    Long getOverdueTasks();

    Long getDueInNext3Days();

    Long getInProgressTasks();

    Double getAvgProgressRate();
}
