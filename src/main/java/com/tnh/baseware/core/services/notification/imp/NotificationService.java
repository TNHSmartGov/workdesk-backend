package com.tnh.baseware.core.services.notification.imp;

import com.tnh.baseware.core.dtos.notification.NotificationDTO;
import com.tnh.baseware.core.dtos.notification.NotificationMessage;
import com.tnh.baseware.core.entities.notification.Notification;
import com.tnh.baseware.core.mappers.notification.INotificationMapper;
import com.tnh.baseware.core.repositories.notification.INotificationRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.notification.INotificationService;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService implements INotificationService {

    INotificationRepository notificationRepository;
    IUserRepository userRepository;
    INotificationMapper notificationMapper;

    @Override
    @Transactional
    public Notification createNotification(NotificationMessage message) {
        try {
            var recipient = userRepository.findById(message.getRecipientId()).orElse(null);

            if (recipient == null) {
                log.warn(LogStyleHelper.warn("Recipient not found for notification: {}"), message.getRecipientId());
                return null;
            }

            var builder = Notification.builder()
                    .recipient(recipient)
                    .type(message.getType())
                    .content(message.getContent())
                    .isRead(false)
                    .dedupKey(message.getDedupKey());

            if (message.getSenderId() != null) {
                userRepository.findById(message.getSenderId())
                        .ifPresent(builder::sender);
            }

            Notification notification = builder.build();
            return notificationRepository.save(notification);

        } catch (DataIntegrityViolationException e) {
            // Idempotency check: Duplicate dedup_key means already processed
            log.warn("Duplicate notification dedup_key detected: {}. Skipping.", message.getDedupKey());
            return null;
        } catch (Exception e) {
            log.error(LogStyleHelper.error("Error creating notification"), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> list(UUID recipientId, boolean unreadOnly, Pageable pageable) {
        Page<Notification> page = unreadOnly
                ? notificationRepository.findByRecipientIdAndIsReadFalse(recipientId, pageable)
                : notificationRepository.findByRecipientId(recipientId, pageable);

        return page.map(notificationMapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Override
    @Transactional
    public int markRead(UUID recipientId, UUID notificationId, Instant readAt) {
        return notificationRepository.markReadById(notificationId, recipientId, readAt);
    }

    @Override
    @Transactional
    public int markAllRead(UUID recipientId, Instant readAt) {
        return notificationRepository.markAllRead(recipientId, readAt);
    }
}
