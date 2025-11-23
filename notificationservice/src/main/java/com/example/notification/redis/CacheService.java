package com.example.notification.redis;

import com.example.notification.model.Notification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class CacheService {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final Duration ttl = Duration.ofMinutes(5); // cache TTL

    public CacheService(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    private String key(long id) {
        return "notification:" + id;
    }

    public void put(Notification notification) {
        try {
            String json = mapper.writeValueAsString(notification);
            redis.opsForValue().set(key(notification.getId()), json, ttl);
        } catch (JsonProcessingException e) {
            // ignore caching on serialization failure (fail-safe)
            e.printStackTrace();
        }
    }

    public Optional<Notification> get(long id) {
        String json = redis.opsForValue().get(key(id));
        if (json == null) return Optional.empty();
        try {
            Notification n = mapper.readValue(json, Notification.class);
            return Optional.of(n);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void evict(long id) {
        redis.delete(key(id));
    }
}
