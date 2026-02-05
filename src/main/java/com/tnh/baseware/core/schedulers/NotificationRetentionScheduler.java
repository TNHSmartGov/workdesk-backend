package com.tnh.baseware.core.schedulers;

import com.tnh.baseware.core.repositories.notification.INotificationRepository;
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

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRetentionScheduler {

    final INotificationRepository notificationRepository;

    @Value("${baseware.core.system.notification-retention-days:90}")
    long retentionDays;

    @Async
    @Scheduled(cron = "${baseware.core.system.notification-retention-cron:0 0 2 * * ?}")
    public void purgeOldNotifications() {
        try {
            Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
            long deleted = notificationRepository.deleteByCreatedDateBefore(cutoff);
            log.debug(LogStyleHelper.debug("Notification retention cleanup deleted {} records."), deleted);
        } catch (Exception ex) {
            log.error(LogStyleHelper.error("Error during notification retention cleanup"), ex);
        }
    }
}
