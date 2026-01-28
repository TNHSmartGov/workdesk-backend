package com.tnh.baseware.core.dtos.dashboard.executive;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourceHealthDTO {
    UUID userId;
    String fullName;
    String avatarUrl;
    long activeTaskCount; // Tasks not Done/Cancelled
    boolean isOverloaded; // > 5 tasks
    String loadStatus; // "NORMAL", "HIGH", "OVERLOADED"
}
