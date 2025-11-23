package com.example.notification.service;

import com.example.notification.model.Notification;
import com.example.notification.redis.CacheService;
import com.example.notification.redis.IdempotencyService;
import com.example.notification.redis.RateLimiter;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.MeterRegistry;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final NotificationRepository repository;
    private final RateLimiter rateLimiter;
    private final IdempotencyService idempotencyService;
    private final CacheService cacheService;
    private final MeterRegistry meterRegistry;


    public String sendNotification(String userId, String msg, String idempotencyKey) {

        log.info("Incoming request userId={}, msgLength={}, idempotencyKey={}",
                userId, msg.length(), idempotencyKey);

        // 1) Rate limiting
        if (!rateLimiter.allowRequest("user:" + userId)) {
            log.warn("RateLimitBlocked userId={}", userId);
            return "Rate limit exceeded. Try later.";
        }

        // 2) Idempotency
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            boolean reserved = idempotencyService.reserve(idempotencyKey, "inprogress");
            if (!reserved) {
                log.info("DuplicateRequest userId={} idempotencyKey={}", userId, idempotencyKey);
                return "Duplicate request detected (idempotency).";
            }
        }

        // 3) Save DB record
        Notification n = new Notification(null, userId, msg, "PROCESSING");
        n = repository.save(n);
        log.info("NotificationCreated id={} userId={}", n.getId(), userId);
        meterRegistry.counter("notifications_created_total").increment();

        // 4) Cache update
        cacheService.put(n);
        log.info("CacheUpdated id={}", n.getId());

        // 5) Publish to Kafka
        kafkaTemplate.send("notifications", n.getId().toString());
        log.info("KafkaEnqueued id={} topic={}", n.getId(), "notifications");

        // 6) Save idempotency → created ID
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyService.reserve(idempotencyKey, String.valueOf(n.getId()));
        }

        return "Queued Notification ID: " + n.getId();
    }

    public Notification getStatus(long id) {
        log.info("StatusCheck id={}", id);
        return cacheService.get(id).orElseGet(() -> {
            log.info("CacheMiss id={}", id);
            return repository.findById(id).orElse(null);
        });
    }
}
