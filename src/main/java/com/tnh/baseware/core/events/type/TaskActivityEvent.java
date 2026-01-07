package com.tnh.baseware.core.events.type;

import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.enums.task.LogActionType;

public record TaskActivityEvent(
        Task task,
        String actor,
        LogActionType actionType,
        String targetField,
        String oldValue,
        String newValue
) {}
