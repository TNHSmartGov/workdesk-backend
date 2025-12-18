package com.tnh.baseware.core.securities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.entities.user.CustomUserDetails;
import com.tnh.baseware.core.properties.SecurityProperties;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.audit.ITrackActivityService;
import com.tnh.baseware.core.services.user.imp.PrivilegeCacheService;
import com.tnh.baseware.core.utils.LogStyleHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LogoutService implements LogoutHandler {

    JwtTokenService jwtTokenService;
    PrivilegeCacheService privilegeCacheService;
    UserDetailsService userDetailsService;
    MessageService messageService;
    SecurityProperties securityProperties;
    SystemProperties systemProperties;
    ITrackActivityService trackActivityService;
    ObjectMapper objectMapper;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {

        final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug(LogStyleHelper.debug("Logout failed: No token found in Authorization header"));
            sendErrorResponse(response);
            return;
        }

        var accessToken = authHeader.substring(7);
        log.debug(LogStyleHelper.debug("JWT Token received: {}"), accessToken);

        try {
            var username = jwtTokenService.extractUsername(accessToken);
            var sessionId = jwtTokenService.extractSessionId(accessToken);

            if (username.isEmpty() || sessionId.isEmpty()) {
                log.debug(LogStyleHelper.debug("Logout failed: Token is invalid"));
                sendErrorResponse(response);
                return;
            }

            var userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username.get());
            if (!jwtTokenService.isTokenValid(accessToken, userDetails)) {
                sendErrorResponse(response);
                log.debug(LogStyleHelper.debug("Logout failed: Token is invalid"));
                return;
            }

            if (securityProperties.getJwt().isAllowMultipleDevices()) {
                // multiple device login
                jwtTokenService.revokeAllValidTokensBySessionId(UUID.fromString(sessionId.get()));
                privilegeCacheService.clearUserPrivilegeAsync(String.valueOf(userDetails.getUser().getId()), sessionId.get());
            } else {
                // single device login
                jwtTokenService.revokeAllValidTokensByUser(userDetails.getUser().getId());
                privilegeCacheService.clearUserPrivilegeAsync(String.valueOf(userDetails.getUser().getId()));
            }

            if (systemProperties.isTrackingActive())
                trackActivityService.trackLogout(request.getRemoteAddr(),
                        request.getHeader("User-Agent"),
                        String.valueOf(username));

            log.debug(LogStyleHelper.debug("Logout successful. Token revoked and cache cleared for user ID: {}"), userDetails.getUser().getId());
        } catch (Exception e) {
            log.debug(LogStyleHelper.debug("Logout failed: Failed to revoke token: {}"), e.getMessage());
            sendErrorResponse(response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void sendErrorResponse(HttpServletResponse response) {
        try {
            var apiMessageDTO = ApiMessageDTO.<String>builder()
                    .result(false)
                    .message(messageService.getMessage("error.unauthorized"))
                    .code(HttpServletResponse.SC_UNAUTHORIZED)
                    .build();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getOutputStream().write(objectMapper.writeValueAsBytes(apiMessageDTO));
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error(LogStyleHelper.error("Logout error: {}"), e.getMessage());
        }
    }
}