package com.example.notification.consumer;

import com.example.notification.model.Notification;
import com.example.notification.redis.CacheService;
import com.example.notification.repository.NotificationRepository;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository repository;
    private final CacheService cacheService;
    private final MeterRegistry meterRegistry;


    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void process(String notificationId) {

        long start = System.currentTimeMillis();
        Long id = Long.parseLong(notificationId);
        Notification n = repository.findById(id).orElseThrow();

        n.setStatus("SENT");
        repository.save(n);

        cacheService.put(n);

        log.info("Kafka processed notification id={} status={}", id, "SENT");

        meterRegistry.counter("notifications_sent_total").increment();
        meterRegistry.timer("consumer_processing_time")
                     .record(System.currentTimeMillis() - start, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
