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
                createCache("recommendations", 1, 20_000),
                createCacheSeconds("song-stats", 20, 20_000),
                createCache("song", 24, 100_000),
                createCache("album", 24, 25_000),
                createCache("playlist", 24, 50_000),
                createCache("allSongs", 1, 1),
                createCache("storage_key", 168, 100_000)
        ));

        return manager;
    }

    private CaffeineCache createCache(String cacheName, int expireHours, int maxSize) {
        return new CaffeineCache(cacheName,
                Caffeine.newBuilder()
                        .expireAfterWrite(expireHours, TimeUnit.HOURS)
                        .maximumSize(maxSize)
                        .build());
    }

    private CaffeineCache createCacheSeconds(String cacheName, int expireSeconds, int maxSize) {
        return new CaffeineCache(cacheName,
                Caffeine.newBuilder()
                        .expireAfterWrite(expireSeconds, TimeUnit.HOURS)
                        .maximumSize(maxSize)
                        .build());
    }
}