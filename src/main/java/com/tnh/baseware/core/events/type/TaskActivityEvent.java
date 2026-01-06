package com.tnh.baseware.core.events.type;

import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.task.LogActionType;

public record TaskActivityEvent(
        Task task,
        User actor,
        LogActionType actionType,
        String targetField,
        String oldValue,
        String newValue
) {}
