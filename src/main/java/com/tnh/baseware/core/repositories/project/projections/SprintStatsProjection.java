package com.tnh.baseware.core.repositories.project.projections;

import java.util.UUID;

public interface SprintStatsProjection {
    UUID getId();

    Long getTotalStoryPoints();

    Long getCompletedStoryPoints();
}
