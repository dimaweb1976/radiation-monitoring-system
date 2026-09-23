package com.example.demo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, BearerFilter bearerFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/measurements", "/api/heartbeat").hasRole("DEVICE")
                        .requestMatchers(HttpMethod.POST, "/api/stations", "/api/detectors").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .addFilterBefore(bearerFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    BearerFilter bearerFilter(@Value("${radiation.auth.admin-token:}") String adminToken,
                              @Value("${radiation.auth.device-tokens:}") String deviceTokens) {
        return new BearerFilter(adminToken, deviceTokens);
    }

    @Bean
    FilterRegistrationBean<BearerFilter> bearerFilterRegistration(BearerFilter filter) {
        FilterRegistrationBean<BearerFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    static class BearerFilter extends OncePerRequestFilter {
        private static final Logger log = LoggerFactory.getLogger(BearerFilter.class);
        private final String adminToken;
        private final Map<Long, String> tokens = new HashMap<>();

        BearerFilter(String adminToken, String configuredTokens) {
            if (!adminToken.isBlank() && adminToken.length() < 16)
                throw new IllegalArgumentException("RADIATION_ADMIN_TOKEN must have at least 16 characters");
            this.adminToken = adminToken;
            if (!configuredTokens.isBlank()) {
                for (String entry : configuredTokens.split(",")) {
                    String[] pair = entry.trim().split(":", 2);
                    if (pair.length != 2 || pair[1].isBlank() || pair[1].length() < 16)
                        throw new IllegalArgumentException("Invalid RADIATION_DEVICE_TOKENS entry");
                    Long id = Long.valueOf(pair[0]);
                    if (tokens.putIfAbsent(id, pair[1]) != null || tokens.values().stream().filter(pair[1]::equals).count() > 1)
                        throw new IllegalArgumentException("Device tokens must be unique");
                }
            }
            if (!adminToken.isBlank() && tokens.containsValue(adminToken))
                throw new IllegalArgumentException("Admin token must differ from device tokens");
            log.info("Bearer authentication configured: admin token {}, {} device token(s)",
                    adminToken.isBlank() ? "absent" : "present", tokens.size());
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String supplied = header.substring(7);
                if (!adminToken.isBlank() && matches(adminToken, supplied)) {
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                            "admin", null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
                } else {
                    for (var entry : tokens.entrySet()) {
                        if (matches(entry.getValue(), supplied)) {
                            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                                    new DevicePrincipal(entry.getKey()), null,
                                    java.util.List.of(new SimpleGrantedAuthority("ROLE_DEVICE"))));
                            break;
                        }
                    }
                }
            }
            chain.doFilter(request, response);
        }

        private boolean matches(String expected, String actual) {
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
        }
    }
}
