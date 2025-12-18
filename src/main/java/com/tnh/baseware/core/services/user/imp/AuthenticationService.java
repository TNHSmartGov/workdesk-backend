package com.tnh.baseware.core.services.user.imp;

import com.tnh.baseware.core.dtos.user.AuthenticationDTO;
import com.tnh.baseware.core.entities.user.CustomUserDetails;
import com.tnh.baseware.core.exceptions.BWCInvalidTokenException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.forms.user.AuthenticationForm;
import com.tnh.baseware.core.properties.SecurityProperties;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.securities.JwtTokenService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.audit.ITrackActivityService;
import com.tnh.baseware.core.utils.BasewareUtils;
import com.tnh.baseware.core.utils.LogStyleHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthenticationService {

        JwtTokenService jwtTokenService;
        CustomUserDetailsService customUserDetailsService;
        AuthenticationManager authenticationManager;
        PrivilegeCacheService privilegeCacheService;
        SecurityProperties securityProperties;
        MessageService messageService;
        ITrackActivityService trackActivityService;
        SystemProperties systemProperties;

        @Transactional
        public AuthenticationDTO login(AuthenticationForm authenticationForm, HttpServletRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                authenticationForm.getUsername(),
                                                authenticationForm.getPassword()));

                var userDetails = (CustomUserDetails) customUserDetailsService
                                .loadUserByUsername(authenticationForm.getUsername());

                if (!securityProperties.getJwt().isAllowMultipleDevices()) {
                        jwtTokenService.revokeAllValidTokensByUser(userDetails.getUser().getId());
                }
                // else {
                // //
                // jwtTokenService.revokeAllValidTokensByUserAndDevice(userDetails.getUser().getId(),
                // // deviceId);
                // log.info("Multiple devices allowed - keeping existing sessions");
                // }

                final var sessionId = UUID.randomUUID();

                var accessToken = jwtTokenService.generateToken(userDetails, request, sessionId)
                                .orElseThrow(() -> new BWCInvalidTokenException(
                                                messageService.getMessage("jwt.token.invalid")));

                var refreshToken = jwtTokenService
                                .generateRefreshToken(userDetails, request, sessionId)
                                .orElseThrow(() -> new BWCInvalidTokenException(
                                                messageService.getMessage("jwt.token.invalid")));

                if (systemProperties.isTrackingActive())
                        trackActivityService.trackLogin(request.getRemoteAddr(),
                                        BasewareUtils.getDevice(request.getHeader("User-Agent")),
                                        authenticationForm.getUsername());

                return AuthenticationDTO.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build();
        }

        @Transactional
        public AuthenticationDTO refreshToken(HttpServletRequest request) {
                var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        log.debug(LogStyleHelper.error("Invalid token format"));
                        throw new BWCInvalidTokenException(messageService.getMessage("jwt.token.invalid"));
                }

                var refreshToken = authHeader.substring(7);

                var username = jwtTokenService.extractUsername(refreshToken)
                                .orElseThrow(() -> new BWCInvalidTokenException(
                                                messageService.getMessage("jwt.token.invalid")));

                var userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
                if (!jwtTokenService.isTokenValid(refreshToken, userDetails)) {
                        log.debug(LogStyleHelper.debug("Invalid token for user {}"), username);
                        throw new BWCInvalidTokenException(messageService.getMessage("jwt.token.invalid"));
                }

                var sessionId = jwtTokenService.extractSessionId(refreshToken)
                                .orElseThrow(() -> new BWCInvalidTokenException(
                                                messageService.getMessage("jwt.token.invalid")));

                if (!securityProperties.getJwt().isAllowMultipleDevices()) {
                        jwtTokenService.revokeAllValidAccessTokensByUser(userDetails.getUser().getId());
                        privilegeCacheService.clearUserPrivilegeAsync(String.valueOf(userDetails.getUser().getId()), sessionId);
                } else {
                        jwtTokenService.revokeAllValidAccessTokensBySessionId(UUID.fromString(sessionId));
                        privilegeCacheService.clearUserPrivilegeAsync(String.valueOf(userDetails.getUser().getId()));
                }

                var newAccessToken = jwtTokenService
                                .generateToken(userDetails, request, UUID.fromString(sessionId))
                                .orElseThrow(() -> new BWCInvalidTokenException(
                                                messageService.getMessage("jwt.token.invalid")));

                privilegeCacheService.clearUserCache(String.valueOf(userDetails.getUser().getId()));
                log.debug(LogStyleHelper.debug("Token refreshed for user {}"), username);

                if (systemProperties.isTrackingActive())
                        trackActivityService.trackRefreshToken(request.getRemoteAddr(),
                                        BasewareUtils.getDevice(request.getHeader("User-Agent")),
                                        username);

                return AuthenticationDTO.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(refreshToken)
                                .build();
        }
}