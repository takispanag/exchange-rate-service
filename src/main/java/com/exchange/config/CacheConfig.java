package com.exchange.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {
    @Value("${exchange.rate.cache.duration-minutes}")
    private int cacheDurationMinutes;

    @Value("${exchange.rate.cache.initial-capacity}")
    private int initialCapacity;

    @Value("${exchange.rate.cache.maximum-size}")
    private int maximumSize;

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(Arrays.asList("exchangeRates", "allRates", "availableCurrencies"));
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .recordStats()
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .expireAfterWrite(cacheDurationMinutes, TimeUnit.MINUTES)
                .removalListener((key, value, cause) ->
                        log.debug("Cache entry removed - Key: {}, Cause: {}", key, cause));
    }
}