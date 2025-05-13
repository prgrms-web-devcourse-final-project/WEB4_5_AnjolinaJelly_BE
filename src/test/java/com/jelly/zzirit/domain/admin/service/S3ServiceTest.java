package com.jelly.zzirit.domain.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class S3ServiceTest {

	@Autowired
	private CommandS3Service commandS3Service;

	/***
	 * S3에 이미지 업로드 테스트, 주석 처리 (테스트할 때 마다 S3에 업로드되므로)
	 */
	// @Test
	// @DisplayName("S3에 이미지 업로드 테스트")
	// public void upload() throws Exception {
	//
	// 	File file = ResourceUtils.getFile("classpath:static/image/Chill.webp");
	// 	System.out.println("파일 경로 확인: " + file.getAbsolutePath());
	//
	// 	// given
	// 	String fileName = "Chill";
	// 	String contentType = "webp"; // 확장자 ex. png, jpg 등
	// 	String filePath = ResourceUtils.getFile("classpath:static/image/Chill.webp").getAbsolutePath();
	//
	// 	MockMultipartFile multipartFile = getMockMultipartFile(fileName, contentType, filePath);
	//
	// 	// when
	// 	String url = s3Service.upload(multipartFile, "test");
	//
	// 	// then
	// 	String uploadedFileName = url.substring(url.lastIndexOf('/') + 1);
	// 	assertThat(uploadedFileName).isNotEmpty();
	// 	System.out.println("업로드된 S3 URL: " + url);
	//
	// 	// https://team03-zzirit-bucket.s3.ap-northeast-2.amazonaws.com/test/cad26fdc-e33d-496a-aeed-3b87a652a47b
	// }


	// private MockMultipartFile getMockMultipartFile(String fileName, String contentType, String path) throws IOException {
	// 	FileInputStream fileInputStream = new FileInputStream(path);
	// 	return new MockMultipartFile(
	// 		"image", // S3Service.upload()에서 기대하는 key
	// 		fileName + "." + contentType,
	// 		"image/" + contentType,
	// 		fileInputStream
	// 	);
	// }

	// @Test
	// @DisplayName("S3 이미지 삭제 단독 테스트")
	// public void delete() {
	// 	// given
	// 	String uploadedUrl = "https://team03-zzirit-bucket.s3.ap-northeast-2.amazonaws.com/test/814b4ec1-d6fd-47ae-8672-1921a9672a4c";
	//
	// 	// when
	// 	s3Service.delete(uploadedUrl);
	//
	// 	// then
	// 	System.out.println("S3 이미지 삭제 완료: " + uploadedUrl);
	// }
}
