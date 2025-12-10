package com.booking.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j token bucket algorithm
 * Protects against brute force attacks, spam, and DDoS
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    // Store buckets per IP address
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // Rate limit configurations for different endpoints
    private static final int AUTH_CAPACITY = 5;           // 5 requests
    private static final Duration AUTH_REFILL = Duration.ofMinutes(1); // per minute

    private static final int BOOKING_CAPACITY = 10;       // 10 requests
    private static final Duration BOOKING_REFILL = Duration.ofMinutes(1); // per minute

    private static final int CANCELLATION_CAPACITY = 3;   // 3 requests
    private static final Duration CANCELLATION_REFILL = Duration.ofMinutes(5); // per 5 minutes

    private static final int DEFAULT_CAPACITY = 100;      // 100 requests
    private static final Duration DEFAULT_REFILL = Duration.ofMinutes(1); // per minute

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIP(request);
        String requestUri = request.getRequestURI();

        // Get or create bucket for this IP
        Bucket bucket = resolveBucket(clientIp, requestUri);

        // Try to consume 1 token
        if (bucket.tryConsume(1)) {
            // Request allowed
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, requestUri);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}"
            );
        }
    }

    /**
     * Resolve bucket based on IP and endpoint
     */
    private Bucket resolveBucket(String clientIp, String requestUri) {
        String key = clientIp + ":" + getEndpointCategory(requestUri);
        return cache.computeIfAbsent(key, k -> createBucket(requestUri));
    }

    /**
     * Create bucket with appropriate limits based on endpoint
     */
    private Bucket createBucket(String requestUri) {
        Bandwidth limit;

        if (requestUri.startsWith("/api/auth/")) {
            // Strict limits on authentication endpoints (brute force protection)
            limit = Bandwidth.builder()
                    .capacity(AUTH_CAPACITY)
                    .refillIntervally(AUTH_CAPACITY, AUTH_REFILL)
                    .build();
            log.debug("Created AUTH rate limit bucket: {} requests per {}", AUTH_CAPACITY, AUTH_REFILL);

        } else if (requestUri.startsWith("/api/booking/") && !requestUri.contains("/cancel/")) {
            // Medium limits on booking creation (spam protection)
            limit = Bandwidth.builder()
                    .capacity(BOOKING_CAPACITY)
                    .refillIntervally(BOOKING_CAPACITY, BOOKING_REFILL)
                    .build();
            log.debug("Created BOOKING rate limit bucket: {} requests per {}", BOOKING_CAPACITY, BOOKING_REFILL);

        } else if (requestUri.contains("/cancel/")) {
            // Very strict limits on cancellation (abuse protection)
            limit = Bandwidth.builder()
                    .capacity(CANCELLATION_CAPACITY)
                    .refillIntervally(CANCELLATION_CAPACITY, CANCELLATION_REFILL)
                    .build();
            log.debug("Created CANCELLATION rate limit bucket: {} requests per {}", CANCELLATION_CAPACITY, CANCELLATION_REFILL);

        } else {
            // Default limits for other endpoints
            limit = Bandwidth.builder()
                    .capacity(DEFAULT_CAPACITY)
                    .refillIntervally(DEFAULT_CAPACITY, DEFAULT_REFILL)
                    .build();
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Categorize endpoint for bucket key
     */
    private String getEndpointCategory(String requestUri) {
        if (requestUri.startsWith("/api/auth/")) {
            return "auth";
        } else if (requestUri.startsWith("/api/booking/") && !requestUri.contains("/cancel/")) {
            return "booking";
        } else if (requestUri.contains("/cancel/")) {
            return "cancellation";
        }
        return "default";
    }

    /**
     * Extract real client IP (considering proxies and load balancers)
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xfHeader.split(",")[0].trim();
        }

        String xrealIp = request.getHeader("X-Real-IP");
        if (xrealIp != null && !xrealIp.isEmpty()) {
            return xrealIp;
        }

        return request.getRemoteAddr();
    }
}
