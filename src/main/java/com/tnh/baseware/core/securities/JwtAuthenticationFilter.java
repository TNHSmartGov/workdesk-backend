package com.tnh.baseware.core.securities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.components.BasewareCoreFilter;
import com.tnh.baseware.core.entities.user.CustomUserDetails;
import com.tnh.baseware.core.properties.SecurityProperties;
import com.tnh.baseware.core.properties.SecurityUriProperties;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(2)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends BasewareCoreFilter {

    UserDetailsService userDetailsService;
    JwtTokenService jwtTokenService;
    SecurityProperties securityProperties;

    public JwtAuthenticationFilter(SecurityUriProperties securityUriProperties,
                                   MessageService messageService,
                                   ObjectMapper objectMapper,
                                   PrivilegeCacheService privilegeCacheService,
                                   UserDetailsService userDetailsService,
                                   JwtTokenService jwtTokenService,
                                   SecurityProperties securityProperties) {
        super(securityUriProperties, messageService, objectMapper, privilegeCacheService);
        this.userDetailsService = userDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        var requestURI = request.getRequestURI();
        if (isBypassUri(requestURI)) {
            log.debug(LogStyleHelper.debug("Bypassing JWT filter for URI: {}"), requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug(LogStyleHelper.debug("User is already authenticated, passing request to next filter"));
            filterChain.doFilter(request, response);
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

            var userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username.get());
            if (!jwtTokenService.isTokenValid(accessToken, userDetails)) {
                log.debug(LogStyleHelper.debug("JWT token is invalid or expired"));
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "error.unauthorized");
                return;
            }

            var privileges = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority).toList();

            if (securityProperties.getJwt().isAllowMultipleDevices()) {
                privilegeCacheService.cachePrivileges(String.valueOf(userDetails.getUser().getId()), sessionId.get(), privileges);
            } else {
                privilegeCacheService.cachePrivileges(String.valueOf(userDetails.getUser().getId()), privileges);
            }

            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (Exception e) {
            log.error(LogStyleHelper.error("Error occurred while processing JWT token: {}"), e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "error.unauthorized");
            return;
        }

        filterChain.doFilter(request, response);
    }
}