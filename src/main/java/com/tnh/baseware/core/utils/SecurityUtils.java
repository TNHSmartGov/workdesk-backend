package com.tnh.baseware.core.utils;

import com.tnh.baseware.core.entities.user.CustomUserDetails;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public final class SecurityUtils {

    private final MessageService messageService;

    public CustomUserDetails currentUserDetails() {
        var context = SecurityContextHolder.getContext();
        if (context == null) {
            throw new BWCValidationException(messageService.getMessage("security.context.null"));
        }

        var auth = context.getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BWCValidationException(messageService.getMessage("user.not.authenticated"));
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails cud)) {
            throw new BWCValidationException(
                    "Expected CustomUserDetails but got: "
                            + principal.getClass().getName()
            );
        }

        return cud;
    }

    public UUID currentOrgId() {
        return currentUserDetails().getOrganizationId();
    }

    public User currentUser() {
        return currentUserDetails().getUser();
    }
}
