package com.jelly.zzirit.domain.admin.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
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

	public String upload(MultipartFile multipartFile, String dirName) throws IOException {
		// (1) MultipartFile을 File로 변환
		File uploadFile = convert(multipartFile)
			.orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환에 실패했습니다."));

		// (2) 파일 이름 중복을 방지하기 위해 UUID 값으로 설정(현재 DB의 길이 제한으로 UUID값만 저장, 필요에 따라 수정 예정)
		String randomName = UUID.randomUUID().toString();
		String fileName = dirName + "/" + randomName;
		String contentType = multipartFile.getContentType();

		// (3)S3에 파일을 업로드. 업로드 완료 여부와 관계 없이 (1)에서 임시 경로에 생성된 파일을 삭제
		try {
			return putS3(uploadFile, fileName, contentType);
		} finally {
			removeNewFile(uploadFile);
		}
	}

	private Optional<File> convert(MultipartFile file) throws IOException {
		// 임시 경로에 file을 생성
		File convertFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());

		// MultipartFile의 내용을 convertFile에 작성
		if (convertFile.createNewFile()) {
			try (FileOutputStream fos = new FileOutputStream(convertFile)) {
				fos.write(file.getBytes());
			}
			return Optional.of(convertFile);
		}
		return Optional.empty();
	}

	private String putS3(File uploadFile, String fileName, String contentType) {
		try {
			// (1) S3에 업로드할 파일의 메타데이터 생성
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(contentType); // 브라우저가 이미지로 인식할 수 있도록 Content-Type 설정
			metadata.setContentLength(uploadFile.length()); // InputStream 사용 시 필수

			// (2) FileInputStream을 이용한 업로드 요청 생성 (File로는 메타데이터 설정 불가)
			PutObjectRequest request = new PutObjectRequest(
				bucket,
				fileName,
				new java.io.FileInputStream(uploadFile),
				metadata
			);

			// (3) S3에 업로드 실행
			s3Client.putObject(request);
		} catch (IOException e) {
			// 예외 발생 시 래핑하여 던짐
			throw new RuntimeException("S3 업로드 중 IOException 발생", e);
		}

		// (4) 업로드된 S3 객체의 URL 반환
		return s3Client.getUrl(bucket, fileName).toString();
	}

	private void removeNewFile(File targetFile) {
		if (targetFile.delete()) {
			log.info("파일이 삭제되었습니다.");
		} else {
			log.info("파일이 삭제되지 못했습니다.");
		}
	}

	public void delete(String imageUrl) {
		String key = extractKeyFromUrl(imageUrl);

		try {
			if (!s3Client.doesObjectExist(bucket, key)) {
				log.info("S3에 존재하지 않는 이미지: {}", imageUrl);
				return;
			}

			s3Client.deleteObject(bucket, key);
			log.info("S3 이미지 삭제 성공: {}", imageUrl);

		} catch (Exception e) {
			log.warn("S3 이미지 삭제 실패: {}", imageUrl, e);
		}
	}

	private String extractKeyFromUrl(String url) {
		// 예: https://bucket-name.s3.amazonaws.com/item-images/uuid0000.jpg
		Pattern pattern = Pattern.compile("https?://[^/]+/(.+)");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return matcher.group(1); // item-images/uuid0000.jpg
		} else {
			throw new InvalidItemException(BaseResponseStatus.INVALID_IMAGE_URL);
		}
	}
}