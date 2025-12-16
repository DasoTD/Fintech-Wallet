package com.example.fintechwallet.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, Bandwidth... limits) {
        return buckets.computeIfAbsent(key, k -> {
            var builder = Bucket4j.builder();
            for (Bandwidth limit : limits) {
                builder.addLimit(limit);
            }
            return builder.build();
        });
    }
}