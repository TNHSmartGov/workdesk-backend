package com.tnh.baseware.core.components;

import com.tnh.baseware.core.exceptions.BasewareCoreException;
import com.tnh.baseware.core.properties.ExternalApiProperties;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.utils.LogStyleHelper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExternalApiClient {

    static final Set<String> ENDPOINTS_WITH_EMPTY_RESPONSE = Set.of(
            "oauth2/revoke");
    WebClient.Builder webClientBuilder;
    MessageService messageService;
    ExternalApiProperties externalApiProperties;
    CircuitBreaker circuitBreaker = CircuitBreaker.of("externalApiCircuitBreaker",
            CircuitBreakerConfig.custom()
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofSeconds(30))
                    .permittedNumberOfCallsInHalfOpenState(10)
                    .slidingWindowSize(100)
                    .minimumNumberOfCalls(10)
                    .build());

    public <T> T callApiSync(String url, HttpMethod method, Map<String, String> headers, Object body,
            Class<T> responseType) {
        log.info(LogStyleHelper.debug("Calling External API - Method: {}, URL: {}"), method, url);
        log.info(LogStyleHelper.info(body != null ? "Request Body: {}" : "No Request Body"), body);
        try {
            return callApi(url, method, headers, body, responseType)
                    .block();
        } catch (BasewareCoreException ex) {
            // Chỉ server errors (5xx) mới được throw ra
            log.debug("External API call completed with status: {}", ex.getErrorCode());
            throw ex;
        }
    }

    private <T> Mono<T> callApi(String url, HttpMethod method, Map<String, String> headers, Object body,
            Class<T> responseType) {
        WebClient client = webClientBuilder.build();

        WebClient.RequestBodySpec requestSpec = client
                .method(method)
                .uri(url)
                .headers(httpHeaders -> {
                    if (headers != null) {
                        log.debug(LogStyleHelper.debug("Adding {} headers to request"), headers.size());
                        headers.forEach(httpHeaders::add);
                    }
                });

        log.debug(LogStyleHelper.debug("Body: {}"), body);

        WebClient.RequestHeadersSpec<?> request = (body != null)
                ? requestSpec.body(BodyInserters.fromValue(body))
                : requestSpec;

        // Mono<T> responseMono = request
        // .retrieve()
        // .onStatus(HttpStatusCode::isError, clientResponse -> {
        // var statusCode = clientResponse.statusCode().value();
        // log.error(LogStyleHelper.error("API error status: {}"), statusCode);

        // return clientResponse.bodyToMono(String.class)
        // .flatMap(errorBody -> {
        // log.error(LogStyleHelper.error("Error response from External API: {} - {}"),
        // statusCode, errorBody);
        // return Mono.error(new BasewareCoreException(
        // messageService.getMessage("external.api.error", statusCode, errorBody),
        // statusCode));
        // });
        // })
        // .bodyToMono(responseType)
        // .timeout(Duration.ofSeconds(externalApiProperties.getRequestTimeoutSeconds()))
        // .retryWhen(createRetrySpec())
        // .doOnSuccess(response -> logSuccess(response, url));
        Mono<T> responseMono = request
                .exchangeToMono(clientResponse -> {
                    var statusCode = clientResponse.statusCode().value();

                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        // Success case
                        return clientResponse.bodyToMono(responseType);
                    }

                    if (statusCode == 404 || statusCode == 401 || statusCode == 403 || statusCode == 400) {
                        // Client errors - treat as business responses
                        log.debug("Client error status {} - treating as business response", statusCode);
                        return clientResponse.bodyToMono(String.class)
                                .switchIfEmpty(Mono.just("{}"))
                                .map(bodyMap -> {
                                    if (responseType == String.class) {
                                        // Nếu body trống, tạo JSON response
                                        if (bodyMap.trim().isEmpty() || bodyMap.equals("{}")) {
                                            return String.format("{\"statusCode\": %d, \"message\": \"Client Error\"}",
                                                    statusCode);
                                        }
                                        return bodyMap;
                                    } else {
                                        // Cho response type khác, throw exception
                                        throw new BasewareCoreException("Client error: " + statusCode, statusCode);
                                    }
                                })
                                .cast(responseType);
                    }

                    if (clientResponse.statusCode().is5xxServerError()) {
                        // Server errors - these should trigger Circuit Breaker
                        log.error(LogStyleHelper.error("Server error status: {}"), statusCode);
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error(LogStyleHelper.error("Error response from External API: {} - {}"),
                                            statusCode, errorBody);
                                    return Mono.error(new BasewareCoreException(
                                            messageService.getMessage("external.api.error", statusCode, errorBody),
                                            statusCode));
                                });
                    }

                    // Other errors
                    log.warn("Unexpected status code: {}", statusCode);
                    return clientResponse.bodyToMono(String.class)
                            .map(bodyMap -> {
                                if (responseType == String.class) {
                                    return String.format("{\"statusCode\": %d, \"message\": \"Unexpected Error\"}",
                                            statusCode);
                                } else {
                                    throw new BasewareCoreException("Unexpected error: " + statusCode, statusCode);
                                }
                            })
                            .cast(responseType);
                })
                .timeout(Duration.ofSeconds(externalApiProperties.getRequestTimeoutSeconds()))
                .retryWhen(createRetrySpec())
                .doOnSuccess(response -> logSuccess(response, url));
        if (externalApiProperties.isCircuitBreakerEnabled()) {
            responseMono = responseMono.transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
        }

        return responseMono;
    }

    private void logSuccess(Object response, String url) {
        if (response != null) {
            log.debug(LogStyleHelper.debug("External API call successful to {}"), url);
        } else if (isEndpointWithExpectedEmptyResponse(url)) {
            log.debug(LogStyleHelper.debug("External API call to {} returned empty response as expected"), url);
        } else {
            log.warn(LogStyleHelper.warn("External API returned null response for {}"), url);
        }
    }

    private Retry createRetrySpec() {
        return Retry
                .backoff(externalApiProperties.getMaxRetryAttempts(),
                        Duration.ofMillis(externalApiProperties.getRetryBackoffMillis()))
                .filter(this::shouldRetry)
                .doBeforeRetry(retrySignal -> log.warn(
                        LogStyleHelper.warn("Retrying External API call after error: {} (attempt: {}/{})"),
                        retrySignal.failure().getMessage(),
                        retrySignal.totalRetries() + 1,
                        externalApiProperties.getMaxRetryAttempts()));
    }

    private boolean isEndpointWithExpectedEmptyResponse(String url) {
        if (url == null) {
            return false;
        }

        return ENDPOINTS_WITH_EMPTY_RESPONSE.stream()
                .anyMatch(url::contains);
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof BasewareCoreException ex) {
            return ex.getErrorCode() >= 500;
        }

        return Exceptions.unwrap(throwable) instanceof TimeoutException ||
                isConnectionError(throwable);
    }

    private boolean isConnectionError(Throwable throwable) {
        Throwable unwrapped = Exceptions.unwrap(throwable);
        String message = Optional.ofNullable(unwrapped.getMessage()).orElse("");

        return unwrapped instanceof java.net.SocketException ||
                message.contains("Connection refused") ||
                message.contains("Connection reset") ||
                message.contains("connection") ||
                message.contains("socket");
    }
}