package com.booking.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * SECURITY: Filter to log security-sensitive requests for monitoring and forensics
 * Logs authentication attempts, admin actions, and suspicious activity
 */
@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String[] SENSITIVE_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/admin/"
    };

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // Only log sensitive endpoints
        if (isSensitiveEndpoint(requestUri)) {
            long startTime = System.currentTimeMillis();

            // Wrap request and response to allow reading body multiple times
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

            try {
                filterChain.doFilter(wrappedRequest, wrappedResponse);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                logRequest(wrappedRequest, wrappedResponse, duration);
                wrappedResponse.copyBodyToResponse();
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isSensitiveEndpoint(String uri) {
        for (String endpoint : SENSITIVE_ENDPOINTS) {
            if (uri.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }

    private void logRequest(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            long duration
    ) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        // SECURITY: Log request details (excluding sensitive data like passwords)
        log.info("SECURITY: {} {} - Status: {} - IP: {} - Duration: {}ms - User-Agent: {}",
                method, uri, status, clientIp, duration,
                userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "unknown");

        // Log warning for failed authentication attempts
        if (uri.contains("/login") && status >= 400) {
            log.warn("SECURITY: Failed login attempt from IP: {} - Status: {}", clientIp, status);
        }

        // Log warning for suspicious activity (multiple rapid requests, etc.)
        if (status == 429) { // Too Many Requests
            log.warn("SECURITY: Rate limit exceeded from IP: {} on endpoint: {}", clientIp, uri);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
