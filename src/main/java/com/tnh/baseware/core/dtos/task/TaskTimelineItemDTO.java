package com.tnh.baseware.core.dtos.task;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskTimelineItemDTO {
    UUID id;
    String type; // "COMMENT" or "ACTIVITY"
    Instant timestamp;
    TaskCommentDTO comment;
    TaskActivityLogDTO activity;
}
