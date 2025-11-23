package com.example.notification.service;

import com.example.notification.model.Notification;
import com.example.notification.redis.CacheService;
import com.example.notification.redis.IdempotencyService;
import com.example.notification.redis.RateLimiter;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final NotificationRepository repository;
    private final RateLimiter rateLimiter;
    private final IdempotencyService idempotencyService;
    private final CacheService cacheService;

    /**
     * Sends notification if rate limit/idempotency allow.
     * idempotencyKey may be null.
     */
    public String sendNotification(String userId, String msg, String idempotencyKey) {

        // 1) Rate limiting per user
        if (!rateLimiter.allowRequest("user:" + userId)) {
            return "Rate limit exceeded. Try later.";
        }

        // 2) Idempotency: if client provides key, avoid duplicates
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            // use some value; storing "inprogress" is enough
            boolean reserved = idempotencyService.reserve(idempotencyKey, "inprogress");
            if (!reserved) {
                // a previous request with same idempotency key already happened
                return "Duplicate request detected (idempotency).";
            }
        }

        // 3) Create DB record
        Notification n = new Notification(null, userId, msg, "PROCESSING");
        n = repository.save(n);

        // 4) Cache the new notification for quick status reads
        cacheService.put(n);

        // 5) Publish to Kafka (async)
        kafkaTemplate.send("notifications", n.getId().toString());

        // 6) Optionally store idempotency -> map to created id
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyService.reserve(idempotencyKey, String.valueOf(n.getId()));
        }

        return "Queued Notification ID: " + n.getId();
    }

    /**
     * Fetch status - first try cache, fall back to DB.
     */
    public Notification getStatus(long id) {
        return cacheService.get(id).orElseGet(() -> repository.findById(id).orElse(null));
    }
}
