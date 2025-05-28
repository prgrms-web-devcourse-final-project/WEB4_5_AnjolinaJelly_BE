package com.jelly.zzirit.domain.order.util;

import java.io.ByteArrayInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.order.dto.StockChangeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncStockHistoryUploader {

	private final AmazonS3 s3Client;
	private final ObjectMapper objectMapper;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	private static final String PREFIX = "logs/stock-history";

	@Async("stockEventExecutor")
	public void upload(StockChangeEvent event) {
		try {
			String date = String.valueOf(event.timestamp());
			String uniqueFileName = event.orderNumber();
			String key = String.join("/", PREFIX, date, uniqueFileName + ".json");

			byte[] jsonBytes = objectMapper.writeValueAsBytes(event);

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(MediaType.APPLICATION_JSON_VALUE);
			metadata.setContentLength(jsonBytes.length);

			PutObjectRequest request = new PutObjectRequest(
				bucket,
				key,
				new ByteArrayInputStream(jsonBytes),
				metadata
			);

			s3Client.putObject(request);
			log.info("S3 재고 로그 저장 완료 - key: {}", key);
		} catch (Exception e) {
			log.error("S3 재고 로그 업로드 실패", e);
		}
	}
}