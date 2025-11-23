package com.example.notification.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class IdempotencyService {

    private final StringRedisTemplate redis;
    private final Duration ttl = Duration.ofMinutes(10);

    public IdempotencyService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String key(String idempotencyKey) {
        return "idem:" + idempotencyKey;
    }

    public boolean reserve(String idempotencyKey, String value) {
        Boolean ok = redis.opsForValue().setIfAbsent(key(idempotencyKey), value, ttl);
        return Boolean.TRUE.equals(ok);
    }

    public Optional<String> get(String idempotencyKey) {
        return Optional.ofNullable(redis.opsForValue().get(key(idempotencyKey)));
    }

    public void remove(String idempotencyKey) {
        redis.delete(key(idempotencyKey));
    }
}
