package com.tnh.baseware.core.services.notification.imp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationSubscriber {

    private final ObjectMapper objectMapper;
    private final LocalSseEmitterManager sseEmitterManager;

    public void onMessage(String message, String channel) {
        try {
            RedisNotificationPublisher.NotificationSignal signal = objectMapper.readValue(message,
                    RedisNotificationPublisher.NotificationSignal.class);
            log.debug("Received Redis notification signal for recipient: {}", signal.getRecipientId());

            sseEmitterManager.pushNotification(signal.getRecipientId(), signal.getNotificationId());

        } catch (IOException e) {
            log.error(LogStyleHelper.error("Failed to parse Redis notification message"), e);
        }
    }
}
