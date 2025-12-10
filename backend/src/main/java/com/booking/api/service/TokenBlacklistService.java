package com.booking.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * SECURITY: Service to manage blacklisted JWT tokens
 * Used to invalidate tokens on logout and prevent token reuse
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * SECURITY: Add a token to the blacklist
     * @param token JWT token to blacklist
     * @param expirationSeconds How long to keep the token in blacklist (should match token expiration)
     */
    public void blacklistToken(String token, long expirationSeconds) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationSeconds, TimeUnit.SECONDS);
        log.info("SECURITY: Token blacklisted for {} seconds", expirationSeconds);
    }

    /**
     * SECURITY: Check if a token is blacklisted
     * @param token JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * SECURITY: Remove a token from blacklist (rarely needed, tokens auto-expire)
     * @param token JWT token to remove from blacklist
     */
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
        log.info("SECURITY: Token removed from blacklist");
    }
}
