package com.soundspace.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        manager.setCaches(Arrays.asList(
                new CaffeineCache("recommendations",
                        Caffeine.newBuilder()
                                .expireAfterWrite(1, TimeUnit.HOURS)
                                .maximumSize(1_000)
                                .build()),

                new CaffeineCache("song",
                        Caffeine.newBuilder()
                                .expireAfterWrite(24, TimeUnit.HOURS)
                                .maximumSize(10_000)
                                .build()),

                new CaffeineCache("album",
                        Caffeine.newBuilder()
                                .expireAfterWrite(24, TimeUnit.HOURS)
                                .maximumSize(10_000)
                                .build()),

                new CaffeineCache("playlist",
                        Caffeine.newBuilder()
                                .expireAfterWrite(24, TimeUnit.HOURS)
                                .maximumSize(10_000)
                                .build())
        ));

        return manager;
    }
}