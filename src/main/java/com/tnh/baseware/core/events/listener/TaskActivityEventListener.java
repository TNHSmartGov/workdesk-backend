package com.tnh.baseware.core.events.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.dtos.notification.NotificationMessage;
import com.tnh.baseware.core.entities.task.TaskActivityLog;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.notification.NotificationType;
import com.tnh.baseware.core.enums.task.LogActionType;
import com.tnh.baseware.core.events.factory.TaskActivityEventFactory;
import com.tnh.baseware.core.events.type.TaskActivityEvent;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.notification.INotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Component
public class TaskActivityEventListener {
    private final ITaskActivityLogRepository taskActivityLogRepository;
    private final ITaskMemberRepository taskMemberRepository;
    private final IUserRepository userRepository;
    private final INotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TaskActivityEvent event) {
        // 1. Save Activity Log
        TaskActivityLog log = TaskActivityLog.builder()
                .task(event.task())
                .actor(userRepository.findByUsername(event.actor()).orElse(null))
                .actionType(event.actionType())
                .targetField(event.targetField())
                .oldValue(event.oldValue())
                .newValue(event.newValue())
                .build();
        taskActivityLogRepository.save(log);

        // 2. Trigger Notification
        createAndPublishNotification(event);
    }

    private void createAndPublishNotification(TaskActivityEvent event) {
        if (event == null) {
            return;
        }

        var actorUser = event.actor() != null
                ? userRepository.findByUsername(event.actor()).orElse(null)
                : null;

        boolean isAssignMember = event.actionType() == LogActionType.ASSIGN_MEMBER
                && "member".equals(event.targetField());
        boolean isAssignRequirement = event
                .actionType() == LogActionType.ASSIGN_REQUIREMENT
                && event.targetField() != null
                && event.targetField().startsWith("requirement:");

        if (isAssignMember || isAssignRequirement) {
            notifySingleRecipient(event, actorUser, NotificationType.TASK_ASSIGNED, event.newValue());
            return;
        }

        switch (event.actionType()) {
            case ADD_COMMENT -> {
                if (isTargetedComment(event)) {
                    notifyMentionedRecipients(event, actorUser, NotificationType.TASK_COMMENT);
                } else {
                    notifyTaskMembers(event, actorUser, NotificationType.TASK_COMMENT);
                }
            }
            case UPDATE_STATUS, CLOSE_TASK -> notifyTaskMembers(event, actorUser, NotificationType.TASK_STATUS_CHANGED);
            case REMOVE_MEMBER -> notifySingleRecipient(event, actorUser,
                    NotificationType.TASK_MEMBER_REMOVED, event.oldValue());
            case UPDATE_MEMBER_ROLE -> notifyFromTargetField(event, actorUser,
                    NotificationType.TASK_MEMBER_ROLE_CHANGED, "role:");
            case UPDATE_MEMBER_STATUS -> notifyFromTargetField(event, actorUser,
                    NotificationType.TASK_MEMBER_STATUS_CHANGED, "status:");
            default -> {
            }
        }
    }

    private boolean isTargetedComment(TaskActivityEvent event) {
        return event.targetField() != null
                && event.targetField().startsWith(TaskActivityEventFactory.COMMENT_MENTION_TARGET_PREFIX);
    }

    private void notifyMentionedRecipients(TaskActivityEvent event, User actorUser,
            NotificationType type) {
        var usernames = extractMentionUsernames(event.targetField());
        if (usernames.isEmpty()) {
            return;
        }
        for (var username : usernames) {
            notifySingleRecipient(event, actorUser, type, username);
        }
    }

    private Set<String> extractMentionUsernames(String targetField) {
        if (targetField == null) {
            return Set.of();
        }
        String prefix = TaskActivityEventFactory.COMMENT_MENTION_TARGET_PREFIX;
        if (!targetField.startsWith(prefix)) {
            return Set.of();
        }
        String raw = targetField.substring(prefix.length()).trim();
        if (raw.isEmpty()) {
            return Set.of();
        }
        Set<String> usernames = new LinkedHashSet<>();
        for (String username : raw.split(",")) {
            String trimmed = username.trim();
            if (!trimmed.isEmpty()) {
                usernames.add(trimmed);
            }
        }
        return usernames;
    }

    private void notifyTaskMembers(TaskActivityEvent event, User actorUser,
            NotificationType type) {
        var members = taskMemberRepository.findByTask_Id(event.task().getId());
        for (var member : members) {
            var recipient = member.getUser();
            if (recipient == null) {
                continue;
            }
            if (actorUser != null && recipient.getId().equals(actorUser.getId())) {
                continue;
            }
            createAndPublish(event, type, recipient, actorUser);
        }
    }

    private void notifySingleRecipient(TaskActivityEvent event, User actorUser,
            NotificationType type, String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        var recipient = userRepository.findByUsername(username).orElse(null);
        if (recipient == null) {
            log.trace("Skip notification: recipient username not found: {}", username);
            return;
        }
        if (actorUser != null && recipient.getId().equals(actorUser.getId())) {
            log.trace("Skip self-notification for user: {}", username);
            return;
        }
        createAndPublish(event, type, recipient, actorUser);
    }

    private void notifyFromTargetField(TaskActivityEvent event, User actorUser,
            NotificationType type, String prefix) {
        if (event.targetField() == null || !event.targetField().startsWith(prefix)) {
            return;
        }
        var username = event.targetField().substring(prefix.length());
        notifySingleRecipient(event, actorUser, type, username);
    }

    private void createAndPublish(TaskActivityEvent event, NotificationType type,
            User recipient,
            User actorUser) {
        String contentJson = buildContent(event);
        if (contentJson == null) {
            return;
        }

        var message = NotificationMessage.builder()
                .recipientId(recipient.getId())
                .senderId(actorUser != null ? actorUser.getId() : null)
                .type(type)
                .content(contentJson)
                .dedupKey(buildDedupKey(event, recipient.getId(), type))
                .build();

        // NotificationService now handles publishing to Redis after transaction commit
        notificationService.createNotification(message);
    }

    private String buildContent(TaskActivityEvent event) {
        try {
            var payload = new HashMap<String, Object>();
            payload.put("taskId", event.task().getId());
            payload.put("title", event.task().getTitle());
            payload.put("actor", event.actor());
            payload.put("actionType", event.actionType().name());
            payload.put("targetField", event.targetField());
            payload.put("oldValue", event.oldValue());
            payload.put("newValue", event.newValue());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification content", e);
            return null;
        }
    }

    private String buildDedupKey(TaskActivityEvent event, java.util.UUID recipientId, NotificationType type) {
        long bucket = System.currentTimeMillis() / 60000;
        return "task_" + type.name().toLowerCase() + "_" + event.task().getId() + "_" + recipientId + "_" + bucket;
    }
}
