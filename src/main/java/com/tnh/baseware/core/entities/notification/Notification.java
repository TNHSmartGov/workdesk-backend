package com.tnh.baseware.core.entities.notification;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.notification.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "IDX_NOTI_RECIPIENT_CREATED", columnList = "recipient_id, created_date DESC"),
        @Index(name = "IDX_NOTI_UNREAD", columnList = "recipient_id, is_read"),
        @Index(name = "UK_DEDUP_KEY", columnList = "dedup_key", unique = true)
})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    NotificationType type;

    @Column(columnDefinition = "text")
    String content; // JSON content

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    Boolean isRead = false;

    Instant readAt;

    @Column(name = "dedup_key", length = 255)
    String dedupKey;
}
