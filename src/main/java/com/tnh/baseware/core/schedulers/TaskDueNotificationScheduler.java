package com.tnh.baseware.core.schedulers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.dtos.notification.NotificationMessage;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.enums.notification.NotificationType;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.services.notification.INotificationService;
import com.tnh.baseware.core.services.notification.imp.RedisNotificationPublisher;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskDueNotificationScheduler {

    final ITaskMemberRepository taskMemberRepository;
    final INotificationService notificationService;
    final RedisNotificationPublisher redisPublisher;
    final ObjectMapper objectMapper;

    @Value("${baseware.core.system.notification-due-soon-hours:48}")
    long dueSoonHours;

    @Async
    @Scheduled(cron = "${baseware.core.system.notification-due-cron:0 0 8 * * ?}")
    public void sendDueNotifications() {
        try {
            Instant now = Instant.now();
            Instant future = now.plus(Duration.ofHours(dueSoonHours));

            notifyMembers(taskMemberRepository.findAssigneesDueSoon(TaskMemberRole.ASSIGNEE, now, future),
                    NotificationType.TASK_DUE_SOON);

            notifyMembers(taskMemberRepository.findAssigneesOverdue(TaskMemberRole.ASSIGNEE, now),
                    NotificationType.TASK_OVERDUE);
        } catch (Exception ex) {
            log.error(LogStyleHelper.error("Error during due/overdue notification scheduler"), ex);
        }
    }

    private void notifyMembers(java.util.List<TaskMember> members, NotificationType type) {
        for (var member : members) {
            if (member.getTask() == null || member.getUser() == null) {
                continue;
            }

            String contentJson = buildContent(member, type);
            if (contentJson == null) {
                continue;
            }

            var message = NotificationMessage.builder()
                    .recipientId(member.getUser().getId())
                    .senderId(null)
                    .type(type)
                    .content(contentJson)
                    .dedupKey(buildDedupKey(member, type))
                    .build();

            var noti = notificationService.createNotification(message);
            if (noti != null) {
                redisPublisher.publish(noti.getId(), noti.getRecipient().getId());
            }
        }
    }

    private String buildContent(TaskMember member, NotificationType type) {
        try {
            var task = member.getTask();
            var payload = new HashMap<String, Object>();
            payload.put("taskId", task.getId());
            payload.put("title", task.getTitle());
            payload.put("dueDate", task.getDueDate());
            payload.put("status", task.getStatus() != null ? task.getStatus().name() : null);
            payload.put("type", type.name());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String buildDedupKey(TaskMember member, NotificationType type) {
        long dayBucket = Instant.now().getEpochSecond() / 86400;
        return "task_due_" + type.name().toLowerCase() + "_" + member.getTask().getId()
                + "_" + member.getUser().getId() + "_" + dayBucket;
    }
}
