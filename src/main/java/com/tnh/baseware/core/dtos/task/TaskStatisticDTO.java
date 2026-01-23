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
    long totalNew; // Status = TODO
    long totalInProgress; // Status = IN_PROGRESS
    long totalReview; // Status = REVIEW
    long totalCompleted; // Status = DONE
    long totalOverdue; // DueDate < Now && Status != DONE
    long totalDueSoon; // DueDate between Now and Now + 3 days && Status != DONE
}
