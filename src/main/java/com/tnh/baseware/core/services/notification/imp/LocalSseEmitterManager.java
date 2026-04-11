package com.tnh.baseware.core.services.notification.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.dtos.notification.NotificationDTO;
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
    ObjectMapper objectMapper;

    public SseEmitter addEmitter(UUID userId) {
        log.info(LogStyleHelper.info("➕ Adding SSE emitter for user: {}"), userId);

        var emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        log.debug("📊 Total emitters for user {}: {}", userId, emitters.get(userId).size());

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(ex -> removeEmitter(userId, emitter));

        // Send initial connection event to establish the SSE connection
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE connection established"));
            log.info(LogStyleHelper.success("✅ SSE connection established for user: {}"), userId);
        } catch (IOException e) {
            log.error(LogStyleHelper.error("❌ Failed to send initial connection event for user: {}"), userId, e);
            removeEmitter(userId, emitter);
        }

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
            log.info(LogStyleHelper.info("🗑️ All emitters removed for user: {}"), userId);
        } else {
            log.debug("📊 Emitters remaining for user {}: {}", userId, list.size());
        }
    }

    public void pushNotification(UUID recipientId, UUID notificationId) {
        var list = emitters.get(recipientId);
        if (list == null || list.isEmpty()) {
            log.debug("⏭️ No active SSE connections for user: {} - Notification: {}", recipientId, notificationId);
            return;
        }

        log.info(LogStyleHelper.success("📬 Pushing notification to {} SSE connections - User: {}, Notification: {}"),
                list.size(), recipientId, notificationId);

        // Fetch DTO inside transaction to handle lazy loading
        NotificationDTO dto = getNotificationDTO(notificationId, recipientId);
        if (dto != null) {
            broadcast(recipientId, list, dto);
            log.debug("✅ Notification broadcast complete");
        } else {
            log.warn(LogStyleHelper.warn("⚠️ Notification not found - ID: {}"), notificationId);
        }
    }

    @Transactional(readOnly = true)
    public NotificationDTO getNotificationDTO(UUID notificationId, UUID recipientId) {
        return notificationRepository.findByIdAndRecipientId(notificationId, recipientId)
                .map(notificationMapper::entityToDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true) // Transactional required here to fetch list and map entities
    public void replayMissed(UUID recipientId, String lastEventId, SseEmitter emitter) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return;
        }

        var parsed = parseLastEventId(lastEventId);
        if (parsed == null) {
            return;
        }

        // Fetch entities inside transaction
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
            // Map to DTO inside transaction
            NotificationDTO dto = notificationMapper.entityToDTO(notification);

            // Send DTO (safe for network)
            if (!sendToEmitter(emitter, dto, notification.getCreatedDate(), notification.getId())) {
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
                } catch (Exception ex) {
                    log.trace("Heartbeat failed for user {}, removing emitter", userId);
                    removeEmitter(userId, emitter);
                }
            }
        });
    }

    private void broadcast(UUID recipientId, List<SseEmitter> list, NotificationDTO dto) {
        for (var emitter : list) {
            if (!sendToEmitter(emitter, dto, dto.getCreatedDate(), dto.getId())) {
                removeEmitter(recipientId, emitter);
            }
        }
    }

    private boolean sendToEmitter(SseEmitter emitter, NotificationDTO dto, Instant createdDate, UUID id) {
        try {
            NotificationDTO dto = notificationMapper.entityToDTO(notification);
            String payload = objectMapper.writeValueAsString(dto);
            String eventId = buildEventId(notification);
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .id(eventId)
                    .data(payload));
            return true;
        } catch (JsonProcessingException ex) {
            log.error(LogStyleHelper.error("Failed to serialize notification payload"), ex);
            return false;
        } catch (IOException ex) {
            log.trace(LogStyleHelper.debug("SSE send failed ({}). Cleaning up emitter."), ex.getMessage());
            return false;
        }
    }

    private String buildEventId(Instant createdDate, UUID id) {
        long timestamp = createdDate != null
                ? createdDate.toEpochMilli()
                : Instant.now().toEpochMilli();
        return timestamp + "_" + id;
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
