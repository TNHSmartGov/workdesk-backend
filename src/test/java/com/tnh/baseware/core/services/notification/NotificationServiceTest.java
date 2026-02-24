package com.tnh.baseware.core.services.notification;

import com.tnh.baseware.core.dtos.notification.NotificationMessage;
import com.tnh.baseware.core.entities.notification.Notification;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.notification.NotificationType;
import com.tnh.baseware.core.mappers.notification.INotificationMapper;
import com.tnh.baseware.core.repositories.notification.INotificationRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.notification.imp.NotificationService;
import com.tnh.baseware.core.services.notification.imp.RedisNotificationPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private INotificationRepository notificationRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private INotificationMapper notificationMapper;

    @Mock
    private RedisNotificationPublisher redisNotificationPublisher;

    @InjectMocks
    private NotificationService notificationService;

    private User recipient;

    @BeforeEach
    void setup() {
        recipient = new User();
        recipient.setId(UUID.randomUUID());
    }

    @Test
    void createNotification_Success() {
        // Arrange
        NotificationMessage message = new NotificationMessage();
        message.setRecipientId(recipient.getId());
        message.setType(NotificationType.SYSTEM_ALERT);
        message.setDedupKey("unique-key-123");
        message.setContent("{}");

        when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Notification result = notificationService.createNotification(message);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(message.getDedupKey(), result.getDedupKey());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void createNotification_DuplicateDedupKey_ReturnsNull() {
        // Arrange
        NotificationMessage message = new NotificationMessage();
        message.setRecipientId(recipient.getId());
        message.setType(NotificationType.SYSTEM_ALERT);
        message.setDedupKey("duplicate-key");

        when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));

        // Simulate DB Unique Constraint violation
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // Act
        Notification result = notificationService.createNotification(message);

        // Assert
        Assertions.assertNull(result, "Should return null when duplicate detected");
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void createNotification_RecipientNotFound_ReturnsNull() {
        // Arrange
        NotificationMessage message = new NotificationMessage();
        message.setRecipientId(UUID.randomUUID());

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        Notification result = notificationService.createNotification(message);

        // Assert
        Assertions.assertNull(result);
        verify(notificationRepository, never()).save(any());
    }
}
