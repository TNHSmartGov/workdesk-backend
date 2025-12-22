package com.tnh.baseware.core.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.entities.user.CustomUserDetails;
import com.tnh.baseware.core.properties.SecurityProperties;
import com.tnh.baseware.core.properties.SecurityUriProperties;
import com.tnh.baseware.core.securities.JwtTokenService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.user.imp.PrivilegeCacheService;
import com.tnh.baseware.core.utils.LogStyleHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(3)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomAuthorizationFilter extends BasewareCoreFilter {

    JwtTokenService jwtTokenService;
    SecurityProperties securityProperties;

    public CustomAuthorizationFilter(SecurityUriProperties securityUriProperties,
            MessageService messageService,
            ObjectMapper objectMapper,
            PrivilegeCacheService privilegeCacheService,
            JwtTokenService jwtTokenService,
            SecurityProperties securityProperties) {
        super(securityUriProperties, messageService, objectMapper, privilegeCacheService);
        this.jwtTokenService = jwtTokenService;
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        var requestURI = request.getRequestURI();
        var requestMethod = request.getMethod();

        if (isBypassUri(requestURI)) {

            log.debug(LogStyleHelper.debug("Bypassing JWT filter for URI: {}"), requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            log.debug(LogStyleHelper.debug("No authentication found, you not access this resource"));
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "error.unauthorized");
            return;
        }

        var tokenOpt = getAccessToken(request);
        if (tokenOpt.isEmpty()) {
            log.debug(LogStyleHelper.debug("No JWT token found, passing request as anonymous"));
            filterChain.doFilter(request, response);
            return;
        }

        var accessToken = tokenOpt.get();
        log.debug(LogStyleHelper.debug("JWT Token received: {}"), accessToken);

        try {
            var username = jwtTokenService.extractUsername(accessToken);
            var sessionId = jwtTokenService.extractSessionId(accessToken);

            if (username.isEmpty() || sessionId.isEmpty()) {
                log.debug(LogStyleHelper.debug("JWT token is invalid or expired"));
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "error.unauthorized");
                return;
            }

            if (!jwtTokenService.isTokenValid(accessToken, userDetails)) {
                log.debug(LogStyleHelper.debug("JWT token is invalid or expired"));
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "error.unauthorized");
                return;
            }

            // the user is super admin, grant access to all resources
            if (Boolean.TRUE.equals(userDetails.getUser().getSuperAdmin())) {
                log.debug(LogStyleHelper.debug("User '{}' is super admin, granting access to all resources"),
                        userDetails.getUsername());
                filterChain.doFilter(request, response);
                return;
            }

            var privileges = securityProperties.getJwt().isAllowMultipleDevices()
                    ? privilegeCacheService.getPrivileges(String.valueOf(userDetails.getUser().getId()),
                            sessionId.get())
                    : privilegeCacheService.getPrivileges(String.valueOf(userDetails.getUser().getId()));

            if (!privilegeCacheService.hasPrivilege(requestMethod, requestURI, privileges)) {
                log.debug(LogStyleHelper.debug("User '{}' does not have access to '{} {}'"), username, requestMethod,
                        requestURI);
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "access.denied");
                return;
            }

            log.debug(LogStyleHelper.debug("User '{}' authorized for '{} {}'"), username, requestMethod, requestURI);
        } catch (Exception e) {
            log.error(LogStyleHelper.error("Error processing JWT token: {}"), e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "error.unauthorized");
            return;
        }

        filterChain.doFilter(request, response);
    }
}