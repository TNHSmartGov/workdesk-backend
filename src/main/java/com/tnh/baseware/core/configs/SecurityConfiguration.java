package com.tnh.baseware.core.configs;

import com.tnh.baseware.core.components.CustomAuthorizationFilter;
import com.tnh.baseware.core.components.TrackActivityFilter;
import com.tnh.baseware.core.properties.CorsProperties;
import com.tnh.baseware.core.properties.SecurityProperties;
import com.tnh.baseware.core.properties.SecurityUriProperties;
import com.tnh.baseware.core.securities.JwtAuthenticationEntryPoint;
import com.tnh.baseware.core.securities.JwtAuthenticationFilter;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfiguration {

    LogoutHandler logoutHandler;
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    CustomAuthorizationFilter customAuthorizationFilter;
    TrackActivityFilter trackActivityFilter;
    JwtAuthenticationFilter jwtAuthenticationFilter;

    CorsProperties corsProperties;
    SecurityUriProperties securityUriProperties;
    SecurityProperties securityProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.debug(LogStyleHelper.debug("Configuring CORS settings"));

        var configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.trace(LogStyleHelper.trace("CORS configuration applied"));
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug(LogStyleHelper.debug("Building security filter chain"));

        var bypassUrls = securityUriProperties.getBypass().toArray(String[]::new);
        var logoutPath = securityProperties.getLogin().getUrlLogout();

        var filterChain = http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(req -> req
                        .requestMatchers(bypassUrls).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(customAuthorizationFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(trackActivityFilter, CustomAuthorizationFilter.class)
                .logout(logout -> logout
                        .logoutUrl(logoutPath)
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler(
                                (request, response, authentication) -> SecurityContextHolder.clearContext()))
                .build();

        log.info(LogStyleHelper.info("Security filter chain configured successfully"));
        return filterChain;
    }
}
