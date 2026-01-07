package com.tnh.baseware.core.events.type;

import com.tnh.baseware.core.constants.FieldChange;
import com.tnh.baseware.core.constants.SystemUser;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.enums.task.LogActionType;
import com.tnh.baseware.core.enums.task.TaskStatus;

import java.util.List;

public final class TaskActivityEventFactory {

    private TaskActivityEventFactory() {}

    public static TaskActivityEvent created(Task task, String actor) {
        return new TaskActivityEvent(
                task,
                actor,
                LogActionType.CREATE_TASK,
                null,
                null,
                task.getTitle()
        );
    }

    public static TaskActivityEvent statusChanged(
            Task task,
            String actor,
            TaskStatus from,
            TaskStatus to
    ) {
        LogActionType type =
                (to == TaskStatus.DONE || to == TaskStatus.CANCELLED)
                        ? LogActionType.CLOSE_TASK
                        : LogActionType.UPDATE_STATUS;

        return new TaskActivityEvent(
                task,
                actor,
                type,
                "status",
                from.name(),
                to.name()
        );
    }

    public static TaskActivityEvent fieldUpdated(
            Task task,
            String actor,
            FieldChange change
    ) {
        return new TaskActivityEvent(
                task,
                actor,
                LogActionType.UPDATE_FIELD,
                change.field(),
                change.oldValue(),
                change.newValue()
        );
    }

    public static List<TaskActivityEvent> fieldUpdatedBatch(
            Task task,
            String actor,
            List<FieldChange> changes
    ) {
        return changes.stream()
                .map(c -> fieldUpdated(task, actor, c))
                .toList();
    }

    public static TaskActivityEvent progressUpdated(
            Task task,
            String actor,
            Integer oldProgress,
            Integer newProgress
    ) {
        LogActionType type =
                newProgress == 100
                        ? LogActionType.MEMBER_SUBMIT
                        : LogActionType.UPDATE_PROGRESS;

        return new TaskActivityEvent(
                task,
                actor,
                type,
                "progress",
                String.valueOf(oldProgress),
                String.valueOf(newProgress)
        );
    }

    public static TaskActivityEvent systemUpdated(
            Task task,
            String field,
            Object oldValue,
            Object newValue
    ) {
        return new TaskActivityEvent(
                task,
                SystemUser.NAME,
                LogActionType.SYSTEM_UPDATE,
                field,
                stringify(oldValue),
                stringify(newValue)
        );
    }

    private static String stringify(Object v) {
        return v == null ? null : v.toString();
    }
}

