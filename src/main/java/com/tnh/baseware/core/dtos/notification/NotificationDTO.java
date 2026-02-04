package com.tnh.baseware.core.dtos.notification;

import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.enums.notification.NotificationType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.RepresentationModel;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationDTO extends RepresentationModel<NotificationDTO> implements Identifiable<UUID> {
    UUID id;
    UUID recipientId;
    UUID senderId;
    String senderName;
    String senderAvatar;
    NotificationType type;
    String content; // JSON
    Boolean isRead;
    Instant readAt;
    Instant createdDate;
}
