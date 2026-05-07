package com.tnh.baseware.core.dtos.task;

import com.tnh.baseware.core.entities.audit.Identifiable;
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
public class TimelineActivityLogDTO implements Identifiable<UUID> {

    UUID id;
    UUID taskId;
    UUID actorId;
    LogActionType actionType;
    String targetField;
    String oldValue;
    String newValue;
    Instant createdDate;
}
