package com.tnh.baseware.core.annotations;

import com.tnh.baseware.core.exceptions.BWCRateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class RateLimitingAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimitingAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(com.tnh.baseware.core.annotations.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        int limit = rateLimit.limit();
        long windowInSeconds = rateLimit.windowInSeconds();
        long lockInSeconds = rateLimit.lockInSeconds();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String uniqueEndpointIdentifier = request.getMethod() + ":" + request.getRequestURI();

        String clientIp = getClientIp(request);
        String rateLimitKey = "rate_limit:" + uniqueEndpointIdentifier + ":" + clientIp;
        String lockKey = "lock:" + uniqueEndpointIdentifier + ":" + clientIp;

        if (Objects.equals(Boolean.TRUE, redisTemplate.hasKey(lockKey))) {
            Long expireTime = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            throw new BWCRateLimitException("Rate limit has been reached. Try again in " + expireTime + " seconds.");
        }
        Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey);
        if (currentCount == null) {
            return joinPoint.proceed();
        }
        if (currentCount == 1) {
            redisTemplate.expire(rateLimitKey, windowInSeconds, TimeUnit.SECONDS);
        }

        if (currentCount > limit) {
            redisTemplate.opsForValue().set(lockKey, "locked", lockInSeconds, TimeUnit.SECONDS);
            redisTemplate.delete(rateLimitKey);
            throw new BWCRateLimitException("Rate limit has been reached. Try again in " + lockInSeconds + " seconds.");
        }

        return joinPoint.proceed();
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || remoteAddr.isEmpty()) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}
