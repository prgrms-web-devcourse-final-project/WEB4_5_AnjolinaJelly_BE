package com.jelly.zzirit.domain.admin.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandS3Service {

	private final AmazonS3 s3Client; // Config에서 s3Client라는 이름으로 등록된 bean 사용

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	public String upload(MultipartFile multipartFile, String dirName) {
		String fileName = dirName + "/" + UUID.randomUUID();

		try (InputStream inputStream = multipartFile.getInputStream()) {
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(multipartFile.getSize());
			metadata.setContentType(multipartFile.getContentType());

			PutObjectRequest request = new PutObjectRequest(bucket, fileName, inputStream, metadata);
			s3Client.putObject(request);

			return s3Client.getUrl(bucket, fileName).toString();
		} catch (IOException e) {
			throw new RuntimeException("S3 업로드 중 오류 발생", e);
		}
	}

	public void delete(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) return;

		try {
			String key = extractKeyFromUrl(imageUrl);
			s3Client.deleteObject(bucket, key);
			log.info("S3 이미지 삭제 완료: {}", imageUrl);
		} catch (InvalidItemException e) {
			log.warn("잘못된 S3 URL 형식입니다. 삭제 생략: {}", imageUrl);
		} catch (Exception e) {
			log.warn("S3 이미지 삭제 중 예외 발생. 삭제 생략: {}", imageUrl, e);
		}
	}

	private String extractKeyFromUrl(String url) {
		// 정확히 team03-zzirit-bucket 주소 패턴만 허용
		Pattern pattern = Pattern.compile("^https://team03-zzirit-bucket\\.s3\\.ap-northeast-2\\.amazonaws\\.com/(.+)$");
		Matcher matcher = pattern.matcher(url);

		if (matcher.find()) {
			return matcher.group(1); // ex: item-images/abc.jpg
		} else {
			throw new InvalidItemException(BaseResponseStatus.INVALID_IMAGE_URL);
		}
	}
}