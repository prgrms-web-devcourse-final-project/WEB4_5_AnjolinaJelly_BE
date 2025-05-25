package com.jelly.zzirit.domain.admin.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	// private Optional<File> convert(MultipartFile file) throws IOException {
	// 	// 임시 경로에 file을 생성
	// 	File convertFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
	//
	// 	// MultipartFile의 내용을 convertFile에 작성
	// 	if (convertFile.createNewFile()) {
	// 		try (FileOutputStream fos = new FileOutputStream(convertFile)) {
	// 			fos.write(file.getBytes());
	// 		}
	// 		return Optional.of(convertFile);
	// 	}
	// 	return Optional.empty();
	// }
	//
	// private String putS3(File uploadFile, String fileName, String contentType) {
	// 	try {
	// 		// S3에 업로드할 파일의 메타데이터 생성
	// 		ObjectMetadata metadata = new ObjectMetadata();
	// 		metadata.setContentType(contentType); // 브라우저가 이미지로 인식할 수 있도록 Content-Type 설정
	// 		metadata.setContentLength(uploadFile.length()); // InputStream 사용 시 필수
	//
	// 		// FileInputStream을 이용한 업로드 요청 생성 (File로는 메타데이터 설정 불가)
	// 		PutObjectRequest request = new PutObjectRequest(
	// 			bucket,
	// 			fileName,
	// 			new java.io.FileInputStream(uploadFile),
	// 			metadata
	// 		);
	//
	// 		// S3에 업로드 실행
	// 		s3Client.putObject(request);
	// 	} catch (IOException e) {
	// 		// 예외 발생 시 래핑하여 던짐
	// 		throw new RuntimeException("S3 업로드 중 IOException 발생", e);
	// 	}
	//
	// 	// 업로드된 S3 객체의 URL 반환
	// 	return s3Client.getUrl(bucket, fileName).toString();
	// }
	//
	// private void removeNewFile(File targetFile) {
	// 	if (targetFile.delete()) {
	// 		log.info("파일이 삭제되었습니다.");
	// 	} else {
	// 		log.info("파일이 삭제되지 못했습니다.");
	// 	}
	// }

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