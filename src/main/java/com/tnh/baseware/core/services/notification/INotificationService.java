package com.tnh.baseware.core.services.notification;

import com.tnh.baseware.core.dtos.notification.NotificationDTO;
import com.tnh.baseware.core.dtos.notification.NotificationMessage;
import com.tnh.baseware.core.entities.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface INotificationService {

    /**
     * Persist notification from internal message payload.
     * Handles idempotency logic internally.
     * 
     * @param message payload
     * @return Saved Notification entity or null if skipped/duplicate
     */
    Notification createNotification(NotificationMessage message);

    Page<NotificationDTO> list(UUID recipientId, boolean unreadOnly, Pageable pageable);

    long countUnread(UUID recipientId);

    int markRead(UUID recipientId, UUID notificationId, Instant readAt);

    int markAllRead(UUID recipientId, Instant readAt);
}
