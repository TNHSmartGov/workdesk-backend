package com.tnh.baseware.core.dtos.notification;

import com.tnh.baseware.core.enums.notification.NotificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationMessage implements Serializable {
    UUID recipientId;

    // Sender info (optional)
    UUID senderId;

    NotificationType type;

    // JSON content structure
    String content;

    // Idempotency fields
    String dedupKey;
}
