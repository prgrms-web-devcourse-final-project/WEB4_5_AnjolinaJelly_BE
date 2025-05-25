package com.jelly.zzirit.domain.admin.controller;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.jelly.zzirit.domain.admin.dto.request.FinishUploadRequest;
import com.jelly.zzirit.domain.admin.dto.request.PreSignedUploadInitiateRequest;
import com.jelly.zzirit.domain.admin.dto.request.PreSignedUrlAbortRequest;
import com.jelly.zzirit.domain.admin.dto.request.PreSignedUrlCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3MultipartUploadController {

    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final String OBJECT_KEY_PREFIX = "uploads/"; // 예: 디렉토리 지정

    // 1. Initiate Multipart Upload
    @PostMapping("/initiate-upload")
    public Map<String, String> initiateUpload(@RequestBody PreSignedUploadInitiateRequest request) {
        String objectKey = OBJECT_KEY_PREFIX + UUID.randomUUID();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(request.getFileSize());
        metadata.setContentType(URLConnection.guessContentTypeFromName(request.getFileType()));

        InitiateMultipartUploadRequest uploadRequest =
                new InitiateMultipartUploadRequest(bucket, objectKey, metadata);

        InitiateMultipartUploadResult result = amazonS3Client.initiateMultipartUpload(uploadRequest);

        // objectKey도 같이 반환
        return Map.of(
                "uploadId", result.getUploadId(),
                "objectKey", objectKey
        );
    }

    // 2. Generate Pre-signed URL for a part
    @PostMapping("/presigned-url")
    public URL generatePresignedUrl(@RequestBody PreSignedUrlCreateRequest request) {
        String objectKey = request.getObjectKey(); // 객체 키는 클라이언트에서 보관하고 있어야 함

        Date expirationTime = Date.from(
                LocalDateTime.now().plusMinutes(10)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        GeneratePresignedUrlRequest presignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, objectKey)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expirationTime);

        presignedUrlRequest.addRequestParameter("uploadId", request.getUploadId());
        presignedUrlRequest.addRequestParameter("partNumber", String.valueOf(request.getPartNumber()));

        return amazonS3Client.generatePresignedUrl(presignedUrlRequest);
    }

    // 3. Complete Multipart Upload
    @PostMapping("/complete-upload")
    public CompleteMultipartUploadResult completeUpload(@RequestBody FinishUploadRequest request) {
        List<PartETag> partETags = request.getParts().stream()
                .map(p -> new PartETag(p.getPartNumber(), p.getETag()))
                .collect(Collectors.toList());

        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                bucket,
                request.getObjectKey(),
                request.getUploadId(),
                partETags
        );

        return amazonS3Client.completeMultipartUpload(completeRequest);
    }

    // 4. Abort Multipart Upload
    @PostMapping("/abort-upload")
    public ResponseEntity<Void> abortUpload(@RequestBody PreSignedUrlAbortRequest request) {
        amazonS3Client.abortMultipartUpload(new AbortMultipartUploadRequest(
                bucket,
                request.getObjectKey(),
                request.getUploadId()
        ));
        return ResponseEntity.ok().build();
    }
}
