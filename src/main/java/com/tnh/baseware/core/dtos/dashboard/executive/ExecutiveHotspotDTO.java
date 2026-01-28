package com.tnh.baseware.core.dtos.dashboard.executive;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExecutiveHotspotDTO {
    long overdueCount;
    long atRiskCount;
    long blockedCount;
}
