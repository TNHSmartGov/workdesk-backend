package com.tnh.baseware.core.services.user.imp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.entities.user.Privilege;
import com.tnh.baseware.core.entities.user.Role;
import com.tnh.baseware.core.exceptions.BWCJsonProcessingException;
import com.tnh.baseware.core.properties.SecurityProperties;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PrivilegeCacheService {

    private static final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;
    SecurityProperties securityProperties;

    private static boolean matchPattern(String apiPattern, String requestURI) {
        var pattern = patternCache.computeIfAbsent(apiPattern, Pattern::compile);
        return pattern.matcher(requestURI).matches();
    }

    private static boolean matchPattern(String apiPattern, String requestMethod, String requestURI) {
        var parts = apiPattern.split(":", 2);
        if (parts.length != 2) return false;

        var method = parts[0];
        var urlPattern = parts[1];

        if (!method.equalsIgnoreCase(requestMethod)) return false;

        return matchPattern(urlPattern, requestURI);
    }

    public void cachePrivileges(String userId, List<String> privileges) {
        var key = securityProperties.getPrivilege().getPrefix() + userId;
        try {
            var json = objectMapper.writeValueAsString(privileges);
            redisTemplate.opsForValue().set(key, json, securityProperties.getPrivilege().getTtl(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error(LogStyleHelper.error("Error caching privileges for user {}"), userId);
            throw new BWCJsonProcessingException("Error caching privileges for user " + userId, e);
        }
    }

    public void cachePrivileges(String userId, String sessionId, List<String> privileges) {
        var key = securityProperties.getPrivilege().getPrefix() + userId + ":" + sessionId;
        try {
            var json = objectMapper.writeValueAsString(privileges);
            redisTemplate.opsForValue().set(key, json, securityProperties.getPrivilege().getTtl(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error(LogStyleHelper.error("Error caching privileges for user {} with session {}"), userId, sessionId);
            throw new BWCJsonProcessingException("Error caching privileges for user " + userId, e);
        }
    }

    public List<String> getPrivileges(String userId) {
        var key = securityProperties.getPrivilege().getPrefix() + userId;
        var json = (String) redisTemplate.opsForValue().get(key);
        if (json == null) return Collections.emptyList();

        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error(LogStyleHelper.error("Error reading privileges from cache for user {}"), userId);
            return Collections.emptyList();
        }
    }

    public List<String> getPrivileges(String userId, String sessionId) {
        var key = securityProperties.getPrivilege().getPrefix() + userId + ":" + sessionId;
        var json = (String) redisTemplate.opsForValue().get(key);
        if (json == null) return Collections.emptyList();

        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error(LogStyleHelper.error("Error reading privileges from cache for user {} and session {}"), userId, sessionId);
            return Collections.emptyList();
        }
    }

    public boolean hasPrivilege(String requestMethod, String requestURI, List<String> privileges) {
        if (privileges == null || privileges.isEmpty()) return false;

        var hasWildcard = privileges.stream()
                .filter(p -> !p.contains(":"))
                .anyMatch(p -> matchPattern(p, requestURI));

        if (hasWildcard) return true;

        return privileges.parallelStream()
                .filter(pattern -> pattern.contains(":"))
                .anyMatch(apiPattern -> matchPattern(apiPattern, requestMethod, requestURI));
    }

    @Async
    public void clearUserPrivilegeAsync(String userId) {
        clearUserCache(userId);
    }

    @Async
    public void clearUserPrivilegeAsync(String userId, String sessionId) {
        clearUserCache(userId, sessionId);
    }

    public void clearUserCache(String userId) {
        var key = securityProperties.getPrivilege().getPrefix() + userId;
        redisTemplate.delete(key);
    }

    public void clearUserCache(String userId, String sessionId) {
        var key = securityProperties.getPrivilege().getPrefix() + userId + ":" + sessionId;
        redisTemplate.delete(key);
    }

    public void clearPatternCache() {
        patternCache.clear();
    }

    public void invalidatePrivilegeCache(Privilege privilege) {
        var users = privilege.getRoles().stream()
                .filter(role -> role.getUsers() != null)
                .flatMap(role -> role.getUsers().stream())
                .toList();

        users.forEach(user -> clearUserPrivilegeAsync(String.valueOf(user.getId())));

        clearPatternCache();
        clearPatternCache();
        log.debug(LogStyleHelper.debug("Deleted privilege cache for privilege {} - affected {} user(s)"), privilege.getId(), users.size());
    }

    public void invalidateAllUserPrivilegesByRole(Role role) {
        if (role.getUsers() == null) return;
        role.getUsers().forEach(user -> clearUserPrivilegeAsync(String.valueOf(user.getId())));
    }

    @Async
    @Scheduled(fixedDelayString = "${baseware.core.system.cache-pattern-clear-interval-ms}") // 2 hours
    public void clearPatternCachePeriodically() {
        clearPatternCache();
        log.debug(LogStyleHelper.debug("Deleted pattern cache"));
    }
}