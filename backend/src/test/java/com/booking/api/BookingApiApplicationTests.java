package com.booking.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.mockito.Mockito;

@SpringBootTest
@ActiveProfiles("test")
class BookingApiApplicationTests {

    @Autowired
    private JavaMailSender javaMailSender;

    // Test simple pour vérifier le contexte
    @Test
    void contextLoads() {
        assert javaMailSender != null;
    }

    // Configuration de test pour fournir un JavaMailSender et RedisTemplate mockés
    @TestConfiguration
    static class TestConfig {
        @Bean
        JavaMailSender javaMailSender() {
            return Mockito.mock(JavaMailSender.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        RedisTemplate<String, String> redisTemplate() {
            return Mockito.mock(RedisTemplate.class);
        }
    }
}
