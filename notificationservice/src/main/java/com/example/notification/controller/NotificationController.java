package com.example.notification.controller;

import com.example.notification.model.Notification;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    public ResponseEntity<String> send(
            @RequestParam String userId,
            @RequestParam String msg,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        String res = service.sendNotification(userId, msg, idempotencyKey);
        if (res.startsWith("Rate limit exceeded") || res.startsWith("Duplicate request")) {
            return ResponseEntity.status(429).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam long id) {
        Notification n = service.getStatus(id);
        if (n == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(n);
    }
}
