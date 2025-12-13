package com.tnh.baseware.core.securities;

import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.properties.SecurityProperties;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginAttemptService {

    IUserRepository userRepository;
    SecurityProperties securityProperties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void loginFailed(User user) {
        var newFailAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newFailAttempts);

        if (newFailAttempts >= securityProperties.getLogin().getMaxAttempts() && !user.getLocked()) {
            user.setLocked(true);
            user.setLockTime(Instant.now());
        }
        userRepository.save(user);
    }

    @Transactional
    public void loginSucceeded(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void unlockWhenTimeExpired(User user) {
        if (user.getLockTime() == null) {
            return;
        }

        var lockTimeInMillis = user.getLockTime().toEpochMilli();

        var currentTimeInMillis = System.currentTimeMillis();
        var lockDurationMillis = securityProperties.getLogin().getLockTimeDuration();

        if (lockTimeInMillis + lockDurationMillis < currentTimeInMillis) {
            user.setLocked(false);
            user.setLockTime(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }
}
