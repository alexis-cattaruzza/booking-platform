package com.booking.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // SECURITY: HTTP Security Headers
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "font-src 'self' data:; " +
                                        "connect-src 'self' https://booking-platform-tau.vercel.app http://localhost:4200; " +
                                        "frame-ancestors 'none'")
                        )
                        .frameOptions(frame -> frame.deny()) // Prevent clickjacking
                        .xssProtection(xss -> xss.disable()) // XSS protection (deprecated, use CSP instead)
                        .contentTypeOptions(contentType -> contentType.disable()) // Prevent MIME sniffing
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000) // 1 year
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics (most specific first)
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/booking/**",
                                "/api/availability/**",
                                "/api/businesses/*/services",
                                "/api/businesses/*/holidays",
                                "/api/businesses/*",
                                "/actuator/health",
                                "/error"
                        ).permitAll()
                        // Business GDPR endpoints - require ROLE_BUSINESS
                        .requestMatchers("/api/businesses/gdpr/**").hasAuthority("ROLE_BUSINESS")
                        // Other business endpoints - require ROLE_BUSINESS
                        .requestMatchers("/api/businesses/**").hasAuthority("ROLE_BUSINESS")
                        // Actuator endpoints secured
                        .requestMatchers("/actuator/**").authenticated()
                        // Other protected endpoints
                        .requestMatchers("/api/services/**").hasAuthority("ROLE_BUSINESS")
                        .requestMatchers("/api/schedules/**").hasAuthority("ROLE_BUSINESS")
                        .requestMatchers("/api/appointments/**").hasAuthority("ROLE_BUSINESS")
                        .requestMatchers("/api/customers/**").hasAuthority("ROLE_BUSINESS")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                // SECURITY: Rate limiting applied BEFORE JWT filter to prevent brute force
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthFilter, RateLimitingFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",           // Local development
                "https://booking-platform-tau.vercel.app"            // Vercel deployments (wildcard works with Pattern)

        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
