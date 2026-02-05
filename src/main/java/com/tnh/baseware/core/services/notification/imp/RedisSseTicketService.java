package com.tnh.baseware.core.services.notification.imp;

import com.tnh.baseware.core.services.notification.ISseTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

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
        return ticket;
    }

    @Override
    public UUID validateAndRemoveTicket(String ticket) {
        String key = TICKET_PREFIX + ticket;
        String userIdStr = stringRedisTemplate.opsForValue().getAndDelete(key);

        if (userIdStr != null) {
            return UUID.fromString(userIdStr);
        }
        return null;
    }
}
