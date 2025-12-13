package com.tnh.baseware.core.configs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.tnh.baseware.core.properties.EmailProperties;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@EnableCaching
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Configurable constants for thread pool
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final String THREAD_PREFIX = "async-task-";

    @Bean
    @Primary
    public FormattingConversionService conversionService() {
        log.debug(LogStyleHelper.debug("Configuring date/time formatting conversion service"));
        var conversionService = new DefaultFormattingConversionService();
        var registrar = new DateTimeFormatterRegistrar();

        registrar.setDateFormatter(DATE_FORMATTER);
        registrar.registerFormatters(conversionService);

        return conversionService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug(LogStyleHelper.debug("Configuring BCrypt password encoder with strength 12"));
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.debug(LogStyleHelper.debug("Configuring authentication manager"));
        return config.getAuthenticationManager();
    }

    @Bean
    public LocalValidatorFactoryBean getValidator(MessageSource messageSource) {
        log.debug(LogStyleHelper.debug("Configuring validation factory with i18n support"));
        var bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        bean.getValidationPropertyMap().put("hibernate.validator.fail_fast", "false");
        return bean;
    }

    @Bean
    public JavaMailSender javaMailSender(EmailProperties emailProperties) {
        log.debug(LogStyleHelper.debug("Configuring Java mail sender for host: {}"), emailProperties.getHost());
        var mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailProperties.getHost());
        mailSender.setPort(emailProperties.getPort());
        mailSender.setUsername(emailProperties.getUsername());
        mailSender.setPassword(emailProperties.getPassword());

        var props = new Properties();
        props.put("mail.transport.protocol", emailProperties.getTransportProtocol());
        props.put("mail.smtp.auth", emailProperties.isSmtpAuth());
        props.put("mail.smtp.starttls.enable", emailProperties.isSmtpStarttlsEnable());
        props.put("mail.debug", emailProperties.isDebug());
        props.put("mail.smtp.connectiontimeout", 5000);
        props.put("mail.smtp.timeout", 5000);
        props.put("mail.smtp.writetimeout", 5000);

        mailSender.setJavaMailProperties(props);

        return mailSender;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        log.debug(LogStyleHelper.debug("Configuring ObjectMapper with custom date/time formatting"));
        var javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
        // Instant serialization will default to ISO-8601 when WRITE_DATES_AS_TIMESTAMPS is disabled

        return builder
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(javaTimeModule)
                .build();
    }

    @Bean
    public Executor taskExecutor() {
        log.debug(LogStyleHelper.debug("Configuring async task executor with core pool size: {}, max pool size: {}"),
                CORE_POOL_SIZE, MAX_POOL_SIZE);

        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_PREFIX);
        return executor;
    }

    @Bean
    @ConditionalOnProperty(prefix = "baseware.core.system.check-camera-devices", name = "enabled", havingValue = "true")
    public ExecutorService cameraCheckExecutorService(
            @Value("${baseware.core.system.check-camera-devices.threads:10}") int threads) {
        return Executors.newFixedThreadPool(threads);
    }

    @Bean
    public ExecutorService playbackProcessingExecutorService(
            @Value("${baseware.core.system.playback.hls.threads:10}") int threads) {
        return Executors.newFixedThreadPool(threads);
    }
}
