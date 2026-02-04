package com.tnh.baseware.core.events.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.dtos.notification.NotificationMessage;
import com.tnh.baseware.core.entities.task.TaskActivityLog;
import com.tnh.baseware.core.enums.notification.NotificationType;
import com.tnh.baseware.core.events.type.TaskActivityEvent;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.notification.INotificationService;
import com.tnh.baseware.core.services.notification.imp.RedisNotificationPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;

@RequiredArgsConstructor
@Slf4j
@Component
public class TaskActivityEventListener {
    private final ITaskActivityLogRepository taskActivityLogRepository;
    private final ITaskMemberRepository taskMemberRepository;
    private final IUserRepository userRepository;
    private final INotificationService notificationService;
    private final RedisNotificationPublisher redisPublisher;
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
        if (event == null || event.newValue() == null) {
            return;
        }

        var actorUser = event.actor() != null
                ? userRepository.findByUsername(event.actor()).orElse(null)
                : null;

        boolean isAssignMember = event.actionType() == com.tnh.baseware.core.enums.task.LogActionType.ASSIGN_MEMBER
                && "member".equals(event.targetField());
        boolean isAssignRequirement = event.actionType() == com.tnh.baseware.core.enums.task.LogActionType.ASSIGN_REQUIREMENT
                && event.targetField() != null
                && event.targetField().startsWith("requirement:");

        if (isAssignMember || isAssignRequirement) {
            notifySingleRecipient(event, actorUser, NotificationType.TASK_ASSIGNED, event.newValue());
            return;
        }

        switch (event.actionType()) {
            case ADD_COMMENT -> notifyTaskMembers(event, actorUser, NotificationType.TASK_COMMENT);
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

    private void notifyTaskMembers(TaskActivityEvent event, com.tnh.baseware.core.entities.user.User actorUser,
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

    private void notifySingleRecipient(TaskActivityEvent event, com.tnh.baseware.core.entities.user.User actorUser,
            NotificationType type, String username) {
        if (username == null) {
            return;
        }
        var recipient = userRepository.findByUsername(username).orElse(null);
        if (recipient == null) {
            log.debug("Skip notification: recipient username not found: {}", username);
            return;
        }
        if (actorUser != null && recipient.getId().equals(actorUser.getId())) {
            return;
        }
        createAndPublish(event, type, recipient, actorUser);
    }

    private void notifyFromTargetField(TaskActivityEvent event, com.tnh.baseware.core.entities.user.User actorUser,
            NotificationType type, String prefix) {
        if (event.targetField() == null || !event.targetField().startsWith(prefix)) {
            return;
        }
        var username = event.targetField().substring(prefix.length());
        notifySingleRecipient(event, actorUser, type, username);
    }

    private void createAndPublish(TaskActivityEvent event, NotificationType type,
            com.tnh.baseware.core.entities.user.User recipient,
            com.tnh.baseware.core.entities.user.User actorUser) {
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

        var noti = notificationService.createNotification(message);
        if (noti != null) {
            redisPublisher.publish(noti.getId(), noti.getRecipient().getId());
        }
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
