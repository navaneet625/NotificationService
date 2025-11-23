package com.example.notification.consumer;

import com.example.notification.model.Notification;
import com.example.notification.redis.CacheService;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository repository;
    private final CacheService cacheService;

    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void process(String notificationId) {

        Long id = Long.parseLong(notificationId);
        Notification n = repository.findById(id).orElseThrow();

        // simulate processing...
        n.setStatus("SENT");
        repository.save(n);

        // update or refresh cache
        cacheService.put(n);

        System.out.println("Processed Notification ID = " + id);
    }
}
