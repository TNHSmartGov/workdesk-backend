package com.tnh.baseware.core.dtos.task;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskStatisticDTO {
    long total;
    long totalNew;
    long totalInProgress;
    long totalReview;
    long totalCompleted;
    long totalOverdue;
    long totalDueSoon;
    long totalActiveProjects;
}
