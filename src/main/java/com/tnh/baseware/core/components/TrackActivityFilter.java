package com.tnh.baseware.core.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.entities.audit.TrackActivity;
import com.tnh.baseware.core.properties.SecurityUriProperties;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.repositories.audit.ITrackActivityRepository;
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
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Order(4)
@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TrackActivityFilter extends BasewareCoreFilter {

    ITrackActivityRepository trackActivityRepository;
    SystemProperties systemProperties;

    public TrackActivityFilter(SecurityUriProperties securityUriProperties,
                               MessageService messageService,
                               ObjectMapper objectMapper,
                               PrivilegeCacheService privilegeCacheService,
                               ITrackActivityRepository trackActivityRepository,
                               SystemProperties systemProperties) {
        super(securityUriProperties, messageService, objectMapper, privilegeCacheService);
        this.trackActivityRepository = trackActivityRepository;
        this.systemProperties = systemProperties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        var requestURI = request.getRequestURI();
        if (!systemProperties.isTrackingActive() || isBypassUri(requestURI)) {
            log.debug(LogStyleHelper.debug("Skipping activity tracking for URI: {} (tracking active: {}, bypass: {})"),
                    requestURI, systemProperties.isTrackingActive(), isBypassUri(requestURI));
            filterChain.doFilter(request, response);
            return;
        }

        var wrappedRequest = new ContentCachingRequestWrapper(request);
        var wrappedResponse = new ContentCachingResponseWrapper(response);
        var startTime = System.currentTimeMillis();

        log.debug(LogStyleHelper.debug("Starting activity tracking for {} request to: {}"),
                request.getMethod(), requestURI);

        var activityBuilder = TrackActivity.builder()
                .requestUrl(requestURI)
                .method(request.getMethod())
                .ipAddress(request.getRemoteAddr())
                .deviceInfo(request.getHeader("User-Agent"))
                .actionDate(Instant.now());

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            activityBuilder.status(wrappedResponse.getStatus());
        } catch (Exception e) {
            log.error(LogStyleHelper.error("Error during request processing: {} for URI: {}"), e.getMessage(), requestURI);
            activityBuilder.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw e;
        } finally {
            try {
                var requestPayload = processUTF8Content(wrappedRequest.getContentAsByteArray());
                activityBuilder.requestPayload(requestPayload);

                var responsePayload = processUTF8Content(wrappedResponse.getContentAsByteArray());
                var responseMessage = extractMessageFromResponse(responsePayload);
                activityBuilder.responsePayload(responseMessage);

                Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                        .ifPresent(auth -> activityBuilder.username(auth.getName()));

                var activity = activityBuilder.build();
                trackActivityRepository.save(activity);

                var processingTime = System.currentTimeMillis() - startTime;
                log.debug(LogStyleHelper.debug("Activity tracked for {} request to: {} by user: {} ({}ms, status: {})"),
                        activity.getMethod(), activity.getRequestUrl(),
                        activity.getUsername() != null ? activity.getUsername() : "anonymous",
                        processingTime, activity.getStatus());
            } catch (Exception e) {
                log.error(LogStyleHelper.error("Failed to save activity tracking data: {}"), e.getMessage());
            } finally {
                wrappedResponse.copyBodyToResponse();
            }
        }
    }

    private String processUTF8Content(byte[] contentBytes) {
        if (contentBytes == null || contentBytes.length == 0) {
            return "";
        }

        try {
            return new String(contentBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn(LogStyleHelper.warn("Failed to decode content as UTF-8: {}"), e.getMessage());
            return new String(contentBytes, StandardCharsets.ISO_8859_1);
        }
    }

    private String extractMessageFromResponse(String content) {
        if (content == null || content.isBlank()) {
            return "Empty response";
        }

        try {
            if (content.length() < 2) {
                return "Empty response content";
            }

            if (content.trim().startsWith("{")) {
                var jsonNode = objectMapper.readTree(content);
                if (jsonNode.has("message")) {
                    String message = jsonNode.get("message").asText();
                    return normalizeText(message);
                }
                return !jsonNode.isEmpty() ? "JSON response with " + jsonNode.size() + " fields" : "Empty JSON";
            }

            var normalizedText = normalizeText(content);
            return normalizedText.length() > 500 ? normalizedText.substring(0, 500) + "... [truncated]"
                    : normalizedText;
        } catch (Exception e) {
            log.debug(LogStyleHelper.debug("Could not parse response as JSON: {}"), e.getMessage());
            return "Non-JSON response (" + content.length() + " chars)";
        }
    }

    private String normalizeText(String input) {
        if (input == null) {
            return "";
        }

        return Normalizer.normalize(input, Normalizer.Form.NFC)
                .replaceAll("\\p{Cntrl}&&[^\n" +
                        "\t]", "");
    }
}
