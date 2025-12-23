package com.soundspace.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.soundspace.enums.Genre;
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
                createCache("storage_key", 168, 200_000),
                createCache("recommendations", 24, 20_000),
                createCacheSeconds("song-stats", 20, 1_500),
                createCache("song", 24, 100_000),
                createCache("playlist", 24, 50_000),
                createCache("album", 24, 25_000),
                createCache("all-songs", 1, 10_000),
                createCache("all-paylists", 1, 5_000),
                createCache("all-albums", 1, 2_500),
                createCache("genre-songs", 1, 2_000 * Genre.values().length),
                createCache("genre-albums", 1, 1_000 * Genre.values().length)
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