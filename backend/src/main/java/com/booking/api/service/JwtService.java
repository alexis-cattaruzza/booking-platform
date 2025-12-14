package com.booking.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 jours par défaut
    private long refreshExpiration;

    /**
     * Validates JWT secret on application startup
     * Ensures minimum 256-bit (32 bytes) secret for HMAC-SHA256 security
     */
    @PostConstruct
    public void validateJwtSecret() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                "SECURITY ERROR: JWT secret is not configured! " +
                "Set JWT_SECRET environment variable with at least 64 hex characters (256 bits). " +
                "Generate with: openssl rand -hex 32"
            );
        }

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                String.format(
                    "SECURITY ERROR: JWT secret is too short (%d bytes). " +
                    "Minimum required: 32 bytes (256 bits). " +
                    "Current: %d bytes. Generate secure secret with: openssl rand -hex 32",
                    secretBytes.length, secretBytes.length
                )
            );
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(
        Map<String, Object> extraClaims,
        UserDetails userDetails,
        long expiration
    ) {
        extraClaims.put("jti", UUID.randomUUID().toString());
        // Add authorities (roles) to JWT claims for proper authorization
        extraClaims.put("authorities", userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList());
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * SECURITY: Get token expiration time in seconds
     * Used for token blacklisting to match Redis TTL
     */
    public long getExpirationTimeInSeconds(String token) {
        Date expiration = extractExpiration(token);
        long now = System.currentTimeMillis();
        long expirationTime = expiration.getTime();
        long remainingTime = (expirationTime - now) / 1000;
        return Math.max(remainingTime, 0);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
