package com.tnh.baseware.core.dtos.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnitPerformanceDTO {
    Long totalTasksCreated;
    Double completionRate;
    Double overdueRate;
}
