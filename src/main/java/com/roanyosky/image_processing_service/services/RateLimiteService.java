package com.roanyosky.image_processing_service.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiteService {
    // Map to store a bucket for each User ID
    private final Map<Long, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(Long userId) {
        return cache.computeIfAbsent(userId, this::createNewBucket);
    }

    private Bucket createNewBucket(Long userId) {
        // Defines the limit: 2 tokens (uploads) per day
        // Using "intervally" means they get exactly 2 new tokens every 24 hours
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(2).refillIntervally(2,Duration.ofDays(1)).build()).build();
    }
}
