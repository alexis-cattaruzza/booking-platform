package com.booking.api.filter;

import com.booking.api.model.AuditLog;
import com.booking.api.service.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * SECURITY: IP-based access control for admin endpoints
 * Restricts admin access to whitelisted IP addresses
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IpWhitelistFilter extends OncePerRequestFilter {

    @Value("${security.admin.ip-whitelist:127.0.0.1,::1,localhost}")
    private String ipWhitelistString;

    private final AuditService auditService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // Only apply IP restriction to admin endpoints
        if (requestUri.startsWith("/api/admin/")) {
            String clientIp = getClientIpAddress(request);
            List<String> whitelist = Arrays.asList(ipWhitelistString.split(","));

            boolean isAllowed = whitelist.stream()
                    .map(String::trim)
                    .anyMatch(ip -> ip.equals(clientIp) ||
                             ip.equals("*") ||
                             clientIp.startsWith("127.") ||
                             clientIp.equals("0:0:0:0:0:0:0:1"));

            if (!isAllowed) {
                log.warn("SECURITY: Unauthorized admin access attempt from IP: {} to {}", clientIp, requestUri);

                // SECURITY: Log unauthorized access attempt
                auditService.logAuditWithUsername(
                        "unauthorized",
                        AuditLog.Actions.UNAUTHORIZED_ACCESS,
                        AuditLog.AuditStatus.FAILURE,
                        request,
                        "Admin endpoint access denied for IP: " + clientIp
                );

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"Your IP address is not authorized to access admin endpoints\"}");
                return;
            }

            log.debug("Admin access allowed from whitelisted IP: {}", clientIp);
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR"
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
