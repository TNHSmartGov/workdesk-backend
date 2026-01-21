package com.tnh.baseware.core.events.factory;

import com.tnh.baseware.core.constants.FieldChange;
import com.tnh.baseware.core.constants.SystemUser;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.task.LogActionType;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.events.type.TaskActivityEvent;

import java.util.List;

public final class TaskActivityEventFactory {

        private TaskActivityEventFactory() {
        }

        public static TaskActivityEvent created(Task task, String actor) {
                return new TaskActivityEvent(
                                task,
                                actor,
                                LogActionType.CREATE_TASK,
                                null,
                                null,
                                task.getTitle());
        }

        public static TaskActivityEvent statusChanged(
                        Task task,
                        String actor,
                        TaskStatus from,
                        TaskStatus to) {
                LogActionType type = (to == TaskStatus.DONE || to == TaskStatus.CANCELLED)
                                ? LogActionType.CLOSE_TASK
                                : LogActionType.UPDATE_STATUS;

                return new TaskActivityEvent(
                                task,
                                actor,
                                type,
                                "status",
                                from.name(),
                                to.name());
        }

        public static TaskActivityEvent fieldUpdated(
                        Task task,
                        String actor,
                        FieldChange change) {
                return new TaskActivityEvent(
                                task,
                                actor,
                                LogActionType.UPDATE_FIELD,
                                change.field(),
                                change.oldValue(),
                                change.newValue());
        }

        public static List<TaskActivityEvent> fieldUpdatedBatch(
                        Task task,
                        String actor,
                        List<FieldChange> changes) {
                return changes.stream()
                                .map(c -> fieldUpdated(task, actor, c))
                                .toList();
        }

        public static TaskActivityEvent progressUpdated(
                        Task task,
                        String actor,
                        Integer oldProgress,
                        Integer newProgress) {
                LogActionType type = newProgress == 100
                                ? LogActionType.MEMBER_SUBMIT
                                : LogActionType.UPDATE_PROGRESS;

                return new TaskActivityEvent(
                                task,
                                actor,
                                type,
                                "progress",
                                String.valueOf(oldProgress),
                                String.valueOf(newProgress));
        }

        public static TaskActivityEvent systemUpdated(
                        Task task,
                        String field,
                        Object oldValue,
                        Object newValue) {
                return new TaskActivityEvent(
                                task,
                                SystemUser.NAME,
                                LogActionType.SYSTEM_UPDATE,
                                field,
                                stringify(oldValue),
                                stringify(newValue));
        }

        public static TaskActivityEvent memberAssigned(Task task, String actor, User member) {
                return new TaskActivityEvent(
                                task, actor, LogActionType.ASSIGN_MEMBER,
                                "member", null, member.getUsername());
        }

        public static TaskActivityEvent memberRemoved(Task task, String actor, User member) {
                return new TaskActivityEvent(
                                task, actor, LogActionType.REMOVE_MEMBER,
                                "member", member.getUsername(), null);
        }

        public static TaskActivityEvent memberRoleChanged(Task task, String actor, User member,
                        TaskMemberRole oldRole, TaskMemberRole newRole) {
                return new TaskActivityEvent(
                                task, actor, LogActionType.UPDATE_MEMBER_ROLE,
                                "role:" + member.getUsername(),
                                oldRole.toString(), newRole.toString());
        }

        public static TaskActivityEvent memberStatusUpdated(Task task, String actor, User member,
                        String oldStatus, String newStatus) {
                return new TaskActivityEvent(
                                task, actor, LogActionType.UPDATE_MEMBER_STATUS,
                                "status:" + member.getUsername(),
                                oldStatus, newStatus);
        }

        public static TaskActivityEvent requirementAdded(Task task, String actor, String content) {
                return new TaskActivityEvent(
                                task, actor, LogActionType.ADD_REQUIREMENT,
                                "requirement", null, truncate(content, 50));
        }

        public static TaskActivityEvent requirementAssigned(Task task, String actor, String content,
                        User oldAssignee, User newAssignee) {
                return new TaskActivityEvent(
                                task, actor, LogActionType.ASSIGN_REQUIREMENT,
                                "requirement:" + truncate(content, 30),
                                oldAssignee != null ? oldAssignee.getUsername() : null,
                                newAssignee.getUsername());
        }

        public static TaskActivityEvent requirementCompleted(Task task, String actor,
                        String content, boolean completed) {
                return new TaskActivityEvent(
                                task, actor,
                                completed ? LogActionType.COMPLETE_REQUIREMENT : LogActionType.UNCOMPLETE_REQUIREMENT,
                                "requirement", null, truncate(content, 50));
        }

        public static TaskActivityEvent commentAdded(Task task, String actor, String content) {
                return new TaskActivityEvent(
                                task, actor, LogActionType.ADD_COMMENT,
                                "comment", null, truncate(content, 50));
        }

        private static String truncate(String s, int maxLen) {
                return s != null && s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
        }

        private static String stringify(Object v) {
                return v == null ? null : v.toString();
        }
}
