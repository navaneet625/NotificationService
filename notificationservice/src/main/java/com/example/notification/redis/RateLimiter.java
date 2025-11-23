package com.example.notification.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {

    private final StringRedisTemplate redis;
    // limit and window can be tuned or moved to application.yml
    private final int limit = 60;            // allowed requests
    private final Duration window = Duration.ofMinutes(1); // per 1 minute

    public RateLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Returns true if allowed, false if rate limit exceeded.
     * key should be unique per user (e.g. "ratelimit:user:123")
     */
    public boolean allowRequest(String key) {
        String redisKey = "ratelimit:" + key;
        Long current = redis.opsForValue().increment(redisKey);
        if (current == null) return false;

        if (current == 1L) {
            // first increment in window -> set expiry
            redis.expire(redisKey, window);
        }

        return current <= limit;
    }

    /**
     * Utility to set custom limit/window if needed.
     */
    public boolean allowRequest(String key, int customLimit, Duration customWindow) {
        String redisKey = "ratelimit:" + key;
        Long current = redis.opsForValue().increment(redisKey);
        if (current == null) return false;

        if (current == 1L) {
            redis.expire(redisKey, customWindow);
        }

        return current <= customLimit;
    }
}
