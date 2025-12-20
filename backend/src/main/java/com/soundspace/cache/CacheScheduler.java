package com.soundspace.cache;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheScheduler {
    private final CacheManager cacheManager;


    @Scheduled(cron = "0 0 * * * *")
    public void clearRecommendationsCache() {
        Cache cache = cacheManager.getCache("recommendations");
        if (cache != null) cache.clear();
        log.info("Zresetowano cache rekomendacji");

    }


    // helpery

    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Wyczyszczono cache: {}", cacheName);
        }
    }

    @PostConstruct
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(this::clearCache);
    }
}
