package com.booking.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * SECURITY: Filter to add security-related HTTP headers to all responses
 * Implements OWASP security best practices
 */
@Component
@Slf4j
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // SECURITY: Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // SECURITY: Enable XSS protection (legacy browsers)
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // SECURITY: Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // SECURITY: Referrer policy - don't leak URLs
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // SECURITY: Permissions policy (formerly Feature-Policy)
        response.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=(), payment=()");

        // SECURITY: Content Security Policy (CSP)
        String csp = "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'";
        response.setHeader("Content-Security-Policy", csp);

        // SECURITY: Strict Transport Security (HSTS)
        // Only enable in production with HTTPS (not on localhost)
        if (!request.getRequestURL().toString().startsWith("http://localhost")) {
            response.setHeader("Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains; preload");
        }

        // SECURITY: Don't expose server information
        response.setHeader("Server", "");

        // SECURITY: Cache control for sensitive data
        if (request.getRequestURI().contains("/api/")) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        filterChain.doFilter(request, response);
    }
}
