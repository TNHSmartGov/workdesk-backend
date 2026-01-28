package com.tnh.baseware.core.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tnh.baseware.core.properties.RedisProperties;
import com.tnh.baseware.core.utils.LogStyleHelper;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;

@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class CacheConfiguration {

    // Default cache TTLs
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    private static final Duration AUTH_TTL = Duration.ofHours(2);
    private static final Duration CONFIG_TTL = Duration.ofHours(24);

    // Connection timeouts
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(3);

    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            RedisProperties redisProperties,
            ClientResources clientResources) {

        log.info(LogStyleHelper.info("Configuring Redis connection to {}:{} with {}"),
                redisProperties.getHost(), redisProperties.getPort(),
                redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()
                        ? "authentication"
                        : "no authentication");

        var config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPort(redisProperties.getPort());

        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
            config.setPassword(redisProperties.getPassword());
        }

        var clientConfig = LettuceClientConfiguration.builder()
                .clientResources(clientResources)
                .commandTimeout(COMMAND_TIMEOUT)
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder()
                                .connectTimeout(CONNECT_TIMEOUT)
                                .keepAlive(true)
                                .build())
                        .timeoutOptions(TimeoutOptions.enabled())
                        .disconnectedBehavior(
                                ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .build())
                .build();

        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.debug(LogStyleHelper.debug("Configuring Redis cache manager with custom TTLs"));

        // Default cache configuration
        var defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "bwc::" + cacheName + "::")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(redisSerializer()));

        // Configure TTLs for specific caches
        var initialCacheConfigurations = new HashMap<String, RedisCacheConfiguration>();
        initialCacheConfigurations.put("authCache", defaultCacheConfig.entryTtl(AUTH_TTL));
        initialCacheConfigurations.put("configCache", defaultCacheConfig.entryTtl(CONFIG_TTL));
        initialCacheConfigurations.put("userCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));

        // Dashboard Caches
        initialCacheConfigurations.put("dashboard_personal", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        initialCacheConfigurations.put("dashboard_unit", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));

        // Task Caches
        initialCacheConfigurations.put("task_timeline", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));
        initialCacheConfigurations.put("task_permissions", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));

        log.debug(LogStyleHelper.debug("Redis Cache TTLs: default={}min, auth={}h, config={}h"),
                DEFAULT_TTL.toMinutes(), AUTH_TTL.toHours(), CONFIG_TTL.toHours());

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(initialCacheConfigurations)
                .transactionAware()
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.debug(LogStyleHelper.debug("Initializing RedisTemplate"));

        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(redisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(redisSerializer());
        template.setEnableTransactionSupport(false);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisSerializer<Object> redisSerializer() {
        var objectMapper = new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .registerModule(new JavaTimeModule());

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(@NonNull RuntimeException exception,
                    @NonNull Cache cache, @NonNull Object key) {
                logCacheError("Error getting from cache", exception, cache, key);
            }

            @Override
            public void handleCachePutError(@NonNull RuntimeException exception,
                    @NonNull Cache cache, @NonNull Object key,
                    Object value) {
                logCacheError("Error putting in cache", exception, cache, key);
            }

            @Override
            public void handleCacheEvictError(@NonNull RuntimeException exception,
                    @NonNull Cache cache, @NonNull Object key) {
                logCacheError("Error evicting from cache", exception, cache, key);
            }

            @Override
            public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
                logCacheError("Error clearing cache", exception, cache, null);
            }

            private void logCacheError(String operation, RuntimeException exception,
                    @NonNull Cache cache, Object key) {
                String cacheName = cache.getName();
                String keyString = key != null ? key.toString() : "null";
                String message = String.format("%s: %s, cache: %s, key: %s",
                        operation, exception.getMessage(), cacheName, keyString);

                log.warn(LogStyleHelper.warn(message));

                // Additional debug logging only if enabled
                if (log.isDebugEnabled()) {
                    log.debug(LogStyleHelper.debug("Redis cache error details"), exception);
                }
            }
        };
    }
}