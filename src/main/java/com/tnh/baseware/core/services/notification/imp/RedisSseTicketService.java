package com.tnh.baseware.core.services.notification.imp;

import com.tnh.baseware.core.services.notification.ISseTicketService;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSseTicketService implements ISseTicketService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String TICKET_PREFIX = "sse:ticket:";
    private static final Duration TICKET_TTL = Duration.ofSeconds(15);

    @Override
    public String generateTicket(UUID userId) {
        String ticket = UUID.randomUUID().toString();
        String key = TICKET_PREFIX + ticket;
        stringRedisTemplate.opsForValue().set(key, userId.toString(), TICKET_TTL);

        log.info(LogStyleHelper.success("🎫 SSE Ticket generated - User: {}, Ticket: {}, TTL: {}s"),
                userId, ticket, TICKET_TTL.getSeconds());

        return ticket;
    }

    @Override
    public UUID validateAndRemoveTicket(String ticket) {
        String key = TICKET_PREFIX + ticket;
        String userIdStr = stringRedisTemplate.opsForValue().getAndDelete(key);

        if (userIdStr != null) {
            UUID userId = UUID.fromString(userIdStr);
            log.info(LogStyleHelper.success("✅ SSE Ticket validated & removed - User: {}, Ticket: {}"),
                    userId, ticket);
            return userId;
        }

        log.warn(LogStyleHelper.warn("❌ SSE Ticket invalid or expired - Ticket: {}"), ticket);
        return null;
    }
}
