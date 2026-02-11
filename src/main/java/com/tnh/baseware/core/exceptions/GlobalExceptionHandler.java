package com.tnh.baseware.core.exceptions;

import com.nimbusds.jose.JOSEException;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.utils.LogStyleHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import java.io.IOException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GlobalExceptionHandler {

        MessageService messageService;

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiMessageDTO<String>> handleGlobalException(Exception ex,
                        HttpServletRequest httpRequest) {
                // Ignored SSE exceptions (client disconnects)
                if (ex instanceof AsyncRequestNotUsableException ||
                                ex.getCause() instanceof AsyncRequestNotUsableException ||
                                (ex instanceof IOException && ex.getMessage().contains("Broken pipe"))) {
                        return null;
                }

                var requestedContentType = getRequestedContentType(httpRequest);

                log.warn(LogStyleHelper.warn("Handling exception: {} with requested content type: {}"),
                                ex.getClass().getSimpleName(), requestedContentType);

                if (isStaticResourceContentType(requestedContentType)) {
                        log.debug(LogStyleHelper
                                        .debug("Static resource request detected, returning appropriate content type"));
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .body(null);
                }

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(ApiMessageDTO.<String>builder()
                                                .data(ex.getMessage())
                                                .result(false)
                                                .message(messageService.getMessage("internal.server.error"))
                                                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                .build());
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiMessageDTO<String>> handleRuntimeException(RuntimeException ex,
                        HttpServletRequest request) {
                var requestedContentType = getRequestedContentType(request);

                if (isStaticResourceContentType(requestedContentType)) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .body(null);
                }

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(ApiMessageDTO.<String>builder()
                                                .data(ex.getMessage())
                                                .result(false)
                                                .message(messageService.getMessage("runtime.error"))
                                                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                .build());
        }

        @ExceptionHandler(BasewareCoreException.class)
        public ResponseEntity<ApiMessageDTO<String>> handleBusiness(BasewareCoreException ex) {
                return ResponseEntity
                                .status(HttpStatus.valueOf(ex.getErrorCode()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(ApiMessageDTO.<String>builder()
                                                .data(ex.getMessage())
                                                .result(false)
                                                .message(ex.getMessage())
                                                .code(ex.getErrorCode())
                                                .build());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiMessageDTO<Object>> handleValidationException(MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(ApiMessageDTO.builder()
                                                .data(errors)
                                                .result(false)
                                                .message(messageService.getMessage("validation.error"))
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .build());
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiMessageDTO<String>> handleAuthenticationException(AuthenticationException ex) {
                return switch (ex) {
                        case BadCredentialsException ignored -> ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(ApiMessageDTO.<String>builder()
                                                        .data(ex.getMessage())
                                                        .result(false)
                                                        .message(messageService.getMessage("auth.bad.credentials"))
                                                        .code(HttpStatus.UNAUTHORIZED.value())
                                                        .build());
                        case UsernameNotFoundException ignored -> ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(ApiMessageDTO.<String>builder()
                                                        .data(ex.getMessage())
                                                        .result(false)
                                                        .message(messageService.getMessage("auth.user.not.found"))
                                                        .code(HttpStatus.UNAUTHORIZED.value())
                                                        .build());
                        case AccountExpiredException ignored -> ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(ApiMessageDTO.<String>builder()
                                                        .data(ex.getMessage())
                                                        .result(false)
                                                        .message(messageService.getMessage("auth.account.expired"))
                                                        .code(HttpStatus.UNAUTHORIZED.value())
                                                        .build());
                        case DisabledException ignored -> ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(ApiMessageDTO.<String>builder()
                                                        .data(ex.getMessage())
                                                        .result(false)
                                                        .message(messageService.getMessage("auth.account.disabled"))
                                                        .code(HttpStatus.UNAUTHORIZED.value())
                                                        .build());
                        case LockedException ignored -> ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(ApiMessageDTO.<String>builder()
                                                        .data(ex.getMessage())
                                                        .result(false)
                                                        .message(messageService.getMessage("auth.account.locked"))
                                                        .code(HttpStatus.UNAUTHORIZED.value())
                                                        .build());
                        case CredentialsExpiredException ignored -> ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(ApiMessageDTO.<String>builder()
                                                        .data(ex.getMessage())
                                                        .result(false)
                                                        .message(messageService.getMessage("auth.credentials.expired"))
                                                        .code(HttpStatus.UNAUTHORIZED.value())
                                                        .build());
                        default -> ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(ApiMessageDTO.<String>builder()
                                                        .data(ex.getMessage())
                                                        .result(false)
                                                        .message(messageService
                                                                        .getMessage("auth.authentication.failed"))
                                                        .code(HttpStatus.UNAUTHORIZED.value())
                                                        .build());
                };
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiMessageDTO<String>> handleAuthorizationException(AccessDeniedException ex) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(ApiMessageDTO.<String>builder()
                                                .data(ex.getMessage())
                                                .result(false)
                                                .message(messageService.getMessage("auth.access.denied"))
                                                .code(HttpStatus.FORBIDDEN.value())
                                                .build());
        }

        @ExceptionHandler(JOSEException.class)
        public ResponseEntity<ApiMessageDTO<String>> handleJwtException(JOSEException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(ApiMessageDTO.<String>builder()
                                                .data(ex.getMessage())
                                                .result(false)
                                                .message(messageService.getMessage("auth.jwt.exception"))
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .build());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiMessageDTO<String>> handleDataIntegrityViolationException(
                        DataIntegrityViolationException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(ApiMessageDTO.<String>builder()
                                                .data(ex.getMessage())
                                                .result(false)
                                                .message(messageService.getMessage("data.integrity.violation"))
                                                .code(HttpStatus.CONFLICT.value())
                                                .build());
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiMessageDTO<Object>> handleConstraintViolation(ConstraintViolationException ex) {
                var errors = new HashMap<String, String>();
                for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
                        var path = violation.getPropertyPath().toString();
                        var field = path.substring(path.lastIndexOf('.') + 1);
                        errors.put(field, violation.getMessage());
                }

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(ApiMessageDTO.builder()
                                                .data(errors)
                                                .result(false)
                                                .message(messageService.getMessage("validation.error"))
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .build());
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiMessageDTO<String>> handleJsonParseError(HttpMessageNotReadableException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(ApiMessageDTO.<String>builder()
                                                .data(ex.getMessage())
                                                .result(false)
                                                .message(messageService.getMessage("json.parse.error"))
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .build());
        }

        private String getRequestedContentType(HttpServletRequest request) {
                var accept = request.getHeader(HttpHeaders.ACCEPT);
                return accept != null ? accept : MediaType.APPLICATION_JSON_VALUE;
        }

        private boolean isStaticResourceContentType(String contentType) {
                return contentType != null && (contentType.contains("text/css") ||
                                contentType.contains("text/javascript") ||
                                contentType.contains("image/") ||
                                contentType.contains("font/") ||
                                contentType.contains("application/font") ||
                                contentType.contains("application/javascript"));
        }
}
