package com.tnh.baseware.core.repositories.user;

import com.tnh.baseware.core.entities.user.Token;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITokenRepository extends IGenericRepository<Token, UUID> {

    Optional<Token> findByJti(String jti);

    List<Token> findAllByUserAndRevokedFalseAndExpiredFalse(User user);

    List<Token> findAllByUserAndDeviceId(User user, String deviceId);

    List<Token> findAllBySessionId(UUID sessionId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Token t WHERE t.expired = true OR t.revoked = true OR t.expiration < :now")
    int deleteByExpiredTrueOrExpirationBefore(@Param("now") Instant now);
}
