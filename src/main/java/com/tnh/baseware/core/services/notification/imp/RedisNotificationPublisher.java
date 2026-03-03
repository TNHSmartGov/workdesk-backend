package com.tnh.baseware.core.services.notification.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.configs.RedisPubSubConfiguration;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisNotificationPublisher {

    StringRedisTemplate stringRedisTemplate;
    ObjectMapper objectMapper;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSignal implements Serializable {
        private UUID notificationId;
        private UUID recipientId;
    }

    public void publish(UUID notificationId, UUID recipientId) {
        try {
            NotificationSignal signal = new NotificationSignal(notificationId, recipientId);
            String message = objectMapper.writeValueAsString(signal);
            stringRedisTemplate.convertAndSend(RedisPubSubConfiguration.NOTIFICATION_TOPIC, message);

            log.info(LogStyleHelper.success("📡 Published to Redis - Topic: {}, Notification: {}, Recipient: {}"),
                    RedisPubSubConfiguration.NOTIFICATION_TOPIC, notificationId, recipientId);
        } catch (JsonProcessingException e) {
            log.error(LogStyleHelper.error("❌ Failed to serialize notification signal"), e);
        }
    }
}
