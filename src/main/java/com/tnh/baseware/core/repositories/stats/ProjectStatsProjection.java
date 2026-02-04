package com.tnh.baseware.core.repositories.stats;

/**
 * Projection interface for project statistics calculation
 * Used in native query aggregation
 */
public interface ProjectStatsProjection {
    Long getTotalProjects();

    Long getActiveProjects();

    Long getOverdueProjects();

    Long getCompletedProjectsToday();
}
