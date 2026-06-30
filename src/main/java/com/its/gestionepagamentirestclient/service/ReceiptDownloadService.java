package com.its.gestionepagamentirestclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptDownloadService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${app.s3.prefix:name}")
    private String prefix;

    public URL createDownloadUrl(String userId, String receiptId, Duration ttl) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is missing");
        }
        if (receiptId == null || receiptId.isBlank()) {
            throw new IllegalArgumentException("receiptId is missing");
        }

        String cleanUserId = userId.trim();
        String cleanReceiptId = receiptId.trim();

        String key = String.format("%s/%s/%s.pdf", prefix, cleanUserId, cleanReceiptId);
        log.info("S3 download key = {}", key);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .responseContentType("application/pdf")
                .responseContentDisposition("attachment; filename=\"receipt-" + cleanReceiptId + ".pdf\"")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url();
    }
}