package com.example.notification.controller;

import com.example.notification.model.Notification;
import com.example.notification.service.NotificationService;
import com.example.notification.service.S3Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;


@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    private final S3Service s3Service;

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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = s3Service.uploadFile(file);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}
