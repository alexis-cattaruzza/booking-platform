package com.booking.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Enables async processing for @Async methods
    // Enables scheduling for @Scheduled methods (for reminder emails)
}
