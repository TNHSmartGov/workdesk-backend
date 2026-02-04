package com.tnh.baseware.core.services.notification.imp;

import com.tnh.baseware.core.dtos.notification.NotificationDTO;
import com.tnh.baseware.core.entities.notification.Notification;
import com.tnh.baseware.core.mappers.notification.INotificationMapper;
import com.tnh.baseware.core.repositories.notification.INotificationRepository;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalSseEmitterManager {

    static long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L;
    static int RESUME_BATCH_SIZE = 50;

    ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    INotificationRepository notificationRepository;
    INotificationMapper notificationMapper;

    public SseEmitter addEmitter(UUID userId) {
        var emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(ex -> removeEmitter(userId, emitter));

        return emitter;
    }

    public void removeEmitter(UUID userId, SseEmitter emitter) {
        var list = emitters.get(userId);
        if (list == null) {
            return;
        }
        list.remove(emitter);
        if (list.isEmpty()) {
            emitters.remove(userId);
        }
    }

    @Transactional(readOnly = true)
    public void pushNotification(UUID recipientId, UUID notificationId) {
        var list = emitters.get(recipientId);
        if (list == null || list.isEmpty()) {
            return;
        }

        notificationRepository.findByIdAndRecipientId(notificationId, recipientId)
                .ifPresent(notification -> broadcast(recipientId, list, notification));
    }

    @Transactional(readOnly = true)
    public void replayMissed(UUID recipientId, String lastEventId, SseEmitter emitter) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return;
        }

        var parsed = parseLastEventId(lastEventId);
        if (parsed == null) {
            return;
        }

        var notifications = notificationRepository.findForResume(
                recipientId,
                parsed.timestamp(),
                PageRequest.of(0, RESUME_BATCH_SIZE));

        for (var notification : notifications) {
            if (notification.getCreatedDate() != null
                    && notification.getCreatedDate().equals(parsed.timestamp())
                    && notification.getId() != null
                    && notification.getId().equals(parsed.notificationId())) {
                continue;
            }
            if (!sendToEmitter(emitter, notification)) {
                removeEmitter(recipientId, emitter);
                break;
            }
        }
    }

    @Scheduled(fixedDelay = 45000)
    public void sendHeartbeat() {
        emitters.forEach((userId, list) -> {
            for (var emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("ping").data("ping"));
                } catch (IOException ex) {
                    removeEmitter(userId, emitter);
                }
            }
        });
    }

    private void broadcast(UUID recipientId, List<SseEmitter> list, Notification notification) {
        for (var emitter : list) {
            if (!sendToEmitter(emitter, notification)) {
                removeEmitter(recipientId, emitter);
            }
        }
    }

    private boolean sendToEmitter(SseEmitter emitter, Notification notification) {
        try {
            NotificationDTO dto = notificationMapper.entityToDTO(notification);
            String eventId = buildEventId(notification);
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .id(eventId)
                    .data(dto));
            return true;
        } catch (IOException ex) {
            log.debug(LogStyleHelper.debug("SSE send failed, cleaning up emitter: {}"), ex.getMessage());
            return false;
        }
    }

    private String buildEventId(Notification notification) {
        long timestamp = notification.getCreatedDate() != null
                ? notification.getCreatedDate().toEpochMilli()
                : Instant.now().toEpochMilli();
        return timestamp + "_" + notification.getId();
    }

    private ParsedLastEvent parseLastEventId(String lastEventId) {
        try {
            var parts = lastEventId.split("_", 2);
            if (parts.length != 2) {
                return null;
            }
            var timestamp = Instant.ofEpochMilli(Long.parseLong(parts[0]));
            var uuid = UUID.fromString(parts[1]);
            return new ParsedLastEvent(timestamp, uuid);
        } catch (Exception ex) {
            log.debug(LogStyleHelper.debug("Invalid Last-Event-ID: {}"), lastEventId);
            return null;
        }
    }

    private record ParsedLastEvent(Instant timestamp, UUID notificationId) {
    }
}
