package com.tnh.baseware.core.dtos.dashboard.executive;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeekVelocityDTO {
    String weekLabel; // e.g. "Week 4" or "22/01 - 28/01"
    long taskCompletedCount;
}
