package com.tnh.baseware.core.dtos.task;

import com.tnh.baseware.core.enums.task.LogActionType;
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
public class ActivityLogDTO {
    UUID id;
    UUID taskId;
    String taskTitle;
    UUID projectId;
    String projectName;

    String actorName;
    String actorAvatar;

    LogActionType actionType;
    String targetField;
    String oldValue;
    String newValue;

    Instant timestamp;
}
