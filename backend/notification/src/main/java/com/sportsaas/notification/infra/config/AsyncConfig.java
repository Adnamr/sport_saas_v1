package com.sportsaas.notification.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration for async email sending with retry support.
 */
@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig {
}
