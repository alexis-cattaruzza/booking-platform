package com.booking.api.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService
 * Tests JWT token generation, validation, and extraction
 */
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails testUser;

    private static final String TEST_SECRET = "testsecrettestsecrettestsecrettestsecrettestsecret"; // Must be 256+ bits
    private static final long TEST_EXPIRATION = 3600000; // 1 hour
    private static final long TEST_REFRESH_EXPIRATION = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Set private fields using reflection
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", TEST_REFRESH_EXPIRATION);

        testUser = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_Success() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void extractUsername_Success() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals("test@example.com", username);
    }

    @Test
    void generateTokenWithExtraClaims_Success() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "BUSINESS_OWNER");
        extraClaims.put("businessId", "123");

        // When
        String token = jwtService.generateToken(extraClaims, testUser);

        // Then
        assertNotNull(token);

        // Verify extra claims are in the token
        Claims claims = jwtService.extractClaim(token, claims1 -> claims1);
        assertEquals("BUSINESS_OWNER", claims.get("role"));
        assertEquals("123", claims.get("businessId"));
    }

    @Test
    void generateRefreshToken_Success() {
        // When
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());

        // Refresh token should have longer expiration
        Date expiration = jwtService.extractClaim(refreshToken, Claims::getExpiration);
        Date now = new Date();
        long timeDiff = expiration.getTime() - now.getTime();

        // Should be approximately 7 days (allow some tolerance)
        assertTrue(timeDiff > TEST_EXPIRATION); // Longer than access token
        assertTrue(timeDiff <= TEST_REFRESH_EXPIRATION + 10000); // Within refresh expiration + 10s tolerance
    }

    @Test
    void isTokenValid_ValidToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WrongUser() {
        // Given
        String token = jwtService.generateToken(testUser);

        UserDetails wrongUser = User.builder()
                .username("wrong@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, wrongUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ExpiredToken() {
        JwtService shortLivedService = new JwtService();
        ReflectionTestUtils.setField(shortLivedService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(shortLivedService, "jwtExpiration", 1L);
        ReflectionTestUtils.setField(shortLivedService, "refreshExpiration", TEST_REFRESH_EXPIRATION);

        String token = shortLivedService.generateToken(testUser);

        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Expect ExpiredJwtException
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class,
                    () -> shortLivedService.isTokenValid(token, testUser));
    }


    @Test
    void extractClaim_ExtractSubject() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Then
        assertEquals("test@example.com", subject);
    }

    @Test
    void extractClaim_ExtractExpiration() {
        // Given
        String token = jwtService.generateToken(testUser);
        Date beforeGeneration = new Date(System.currentTimeMillis() - 1000);
        Date afterExpectedExpiration = new Date(System.currentTimeMillis() + TEST_EXPIRATION + 1000);

        // When
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(beforeGeneration));
        assertTrue(expiration.before(afterExpectedExpiration));
    }

    @Test
    void extractClaim_ExtractIssuedAt() {
        // Given
        Date before = new Date(System.currentTimeMillis() - 1000);
        String token = jwtService.generateToken(testUser);
        Date after = new Date(System.currentTimeMillis() + 1000);

        // When
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        // Then
        assertNotNull(issuedAt);
        assertTrue(issuedAt.after(before) || issuedAt.equals(before));
        assertTrue(issuedAt.before(after) || issuedAt.equals(after));
    }

    @Test
    void generateToken_DifferentUsersProduceDifferentTokens() {
        // Given
        UserDetails user1 = User.builder()
                .username("user1@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        UserDetails user2 = User.builder()
                .username("user2@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        // Then
        assertNotEquals(token1, token2);
        assertEquals("user1@example.com", jwtService.extractUsername(token1));
        assertEquals("user2@example.com", jwtService.extractUsername(token2));
    }

    @Test
    void generateToken_MultipleCallsProduceDifferentTokens() {
        // When - Generate tokens at slightly different times
        String token1 = jwtService.generateToken(testUser);

        try {
            Thread.sleep(100); // Small delay to ensure different issuedAt
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtService.generateToken(testUser);

        // Then - Tokens should be different due to different issuedAt times
        assertNotEquals(token1, token2);

        // But both should be valid for the same user
        assertTrue(jwtService.isTokenValid(token1, testUser));
        assertTrue(jwtService.isTokenValid(token2, testUser));
    }

    @Test
    void extractUsername_FromTokenWithExtraClaims() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customField", "customValue");
        String token = jwtService.generateToken(extraClaims, testUser);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals("test@example.com", username);
    }

    @Test
    void tokenExpiration_AccessTokenShorterThanRefreshToken() {
        // Given
        String accessToken = jwtService.generateToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // When
        Date accessExpiration = jwtService.extractClaim(accessToken, Claims::getExpiration);
        Date refreshExpiration = jwtService.extractClaim(refreshToken, Claims::getExpiration);

        // Then
        assertTrue(refreshExpiration.after(accessExpiration),
                "Refresh token should expire after access token");
    }

    @Test
    void extractClaim_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    void extractClaim_EmptyToken() {
        // Given
        String emptyToken = "";

        // When & Then
        assertThrows(Exception.class, () -> jwtService.extractUsername(emptyToken));
    }

    @Test
    void isTokenValid_TokenFromDifferentSecret() {
        // Given
        String token = jwtService.generateToken(testUser);

        // Create new service with different secret
        JwtService differentSecretService = new JwtService();
        ReflectionTestUtils.setField(differentSecretService, "secret",
                "differentsecretdifferentsecretdifferentsecretdifferent");
        ReflectionTestUtils.setField(differentSecretService, "jwtExpiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(differentSecretService, "refreshExpiration", TEST_REFRESH_EXPIRATION);

        // When & Then - Should throw exception when trying to validate with wrong secret
        assertThrows(Exception.class, () ->
                differentSecretService.isTokenValid(token, testUser));
    }

    @Test
    void generateToken_WithValidUsername() {
        UserDetails user = User.builder()
                .username("valid@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertEquals("valid@example.com", jwtService.extractUsername(token));
    }

    @Test
    void extractClaim_CustomClaimExtraction() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", 12345);
        extraClaims.put("roles", "ADMIN,USER");

        String token = jwtService.generateToken(extraClaims, testUser);

        // When
        Integer userId = jwtService.extractClaim(token, claims -> claims.get("userId", Integer.class));
        String roles = jwtService.extractClaim(token, claims -> claims.get("roles", String.class));

        // Then
        assertEquals(12345, userId);
        assertEquals("ADMIN,USER", roles);
    }
}
