package com.tnh.baseware.core.components;

import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import com.tnh.baseware.core.repositories.user.ITokenRepository;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenCleanupScheduler {

    ITokenRepository tokenRepository;

    @Async
    @Scheduled(cron = "${baseware.core.system.token-clear-interval}")
    public void cleanExpiredAndRevokedTokens() {
        try {
            var deletedCount = tokenRepository.deleteByExpiredTrueOrExpirationBefore(Instant.now());
            log.debug(LogStyleHelper.debug("Token cleanup task ran successfully. Deleted {} expired/revoked tokens."), deletedCount);
        } catch (Exception ex) {
            log.error(LogStyleHelper.error("Error during token cleanup: {}"), ex.getMessage());
            throw new BWCGenericRuntimeException("Error during token cleanup", ex);
        }
    }
}
