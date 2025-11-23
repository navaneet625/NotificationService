package com.example.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) throws IOException {

        String key = "uploads/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        log.info("S3UploadStart bucket={} key={} size={}",
                bucket, key, file.getSize());

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request,
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

            String url = "https://" + bucket + ".s3.amazonaws.com/" + key;

            log.info("S3UploadSuccess url={}", url);

            return url;

        } catch (Exception e) {
            log.error("S3UploadFailed bucket={} key={} error={}", bucket, key, e.getMessage());
            throw e;
        }
    }
}
