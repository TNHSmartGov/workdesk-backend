package com.tnh.baseware.core.dtos.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnitWorkloadDTO {
    UUID userId;
    String fullName;
    String avatar;
    Long taskCount;
    Long overdueCount;
}
