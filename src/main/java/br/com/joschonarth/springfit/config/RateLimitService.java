package br.com.joschonarth.springfit.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimitService {

    private final Cache<String, Bucket> authBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    private final Cache<String, Bucket> apiBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    private final int authCapacity;
    private final long authRefillSeconds;
    private final int apiCapacity;
    private final long apiRefillSeconds;

    public RateLimitService(
            @Value("${rate-limit.auth.capacity}") int authCapacity,
            @Value("${rate-limit.auth.refill-period-seconds}") long authRefillSeconds,
            @Value("${rate-limit.api.capacity}") int apiCapacity,
            @Value("${rate-limit.api.refill-period-seconds}") long apiRefillSeconds) {
        this.authCapacity = authCapacity;
        this.authRefillSeconds = authRefillSeconds;
        this.apiCapacity = apiCapacity;
        this.apiRefillSeconds = apiRefillSeconds;
    }

    public Bucket resolveAuthBucket(String key) {
        return authBuckets.get(key, k -> newBucket(authCapacity, authRefillSeconds));
    }

    public Bucket resolveApiBucket(String key) {
        return apiBuckets.get(key, k -> newBucket(apiCapacity, apiRefillSeconds));
    }

    private Bucket newBucket(int capacity, long refillSeconds) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillIntervally(capacity, Duration.ofSeconds(refillSeconds))
                        .build())
                .build();
    }
}