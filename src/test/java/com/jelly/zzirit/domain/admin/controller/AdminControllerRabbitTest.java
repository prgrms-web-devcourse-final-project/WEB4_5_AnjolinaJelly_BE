package com.jelly.zzirit.domain.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.admin.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.admin.dto.request.ItemUpdateRequest;
import com.jelly.zzirit.domain.admin.service.CommandS3Service;
import com.jelly.zzirit.global.support.RabbitTestMemberConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerRabbitTest extends RabbitTestMemberConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommandS3Service commandS3Service;

    @BeforeEach
    void setUpMockS3() throws IOException {
        // Mock S3Service 응답 설정
        given(commandS3Service.upload(any(MultipartFile.class), any(String.class)))
                .willReturn("https://fake-s3-url.com/fake-image.jpg");
    }

    @Test
    @DisplayName("상품 목록 조회 (성공)")
    void getItems_shouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/items")
                .cookie(getAdminAccessTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("상품 등록 요청 시 정상 응답")
    void createItem_shouldReturnSuccess() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest(
                "테스트상품",
                100,
                new BigDecimal("9900"),
                1L,
                1L,
                "https://example.com/image.jpg"
        );
        mockMvc.perform(post("/api/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(getAdminAccessTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("상품 수정 요청 시 정상 응답")
    void updateItem_shouldReturnSuccess() throws Exception {
        ItemUpdateRequest request = new ItemUpdateRequest(50, new BigDecimal("8900"));

        mockMvc.perform(put("/api/admin/items/{itemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .cookie(getAdminAccessTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("상품 이미지 업로드 성공 (Mock S3)")
    void uploadImage_shouldReturnImageUrl() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/items/image")
                        .file(image)
                        .cookie(getAdminAccessTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.imageUrl")
                        .value("https://fake-s3-url.com/fake-image.jpg"));
    }

    @Test
    @DisplayName("상품 이미지 수정 성공 (Mock S3)")
    void updateImage_shouldReplaceImage() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "new-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/items/{itemId}/image", 1L)
                        .file(image)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .cookie(getAdminAccessTokenCookie())) // multipart PUT 요청 설정
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.imageUrl")
                        .value("https://fake-s3-url.com/fake-image.jpg"));
    }

    @Test
    @DisplayName("상품 삭제 요청 시 정상 응답")
    void deleteItem_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/items/{itemId}", 1L)
                        .cookie(getAdminAccessTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}



//    @Autowired
//    private JwtUtil jwtUtil; // 테스트용 JWT 발급 유틸
//
//    @Test
//    void 관리자_상품_조회_API_문서() {
//        // 1. 테스트용 사용자 정보
//        Long userId = 1L;
//        Role role = Role.ROLE_USER;
//
//        // 2. JWT access token 발급 (1시간 유효)
//        String accessToken = jwtUtil.createJwt("access", userId, role, 3600);
//        System.out.println("accessToken: " + accessToken); // 디버깅용 출력
//
//        // 3. RestAssured spec 설정 + API 호출 + RestDocs 문서화
//        String responseBody = this.spec
//                // 인증 토큰 설정
//                .header("Authorization", "Bearer " + accessToken)
//                // 응답 필드에 대한 문서화 필터 등록 (OpenAPI + RestDocs 연동)
//                .filter(OpenApiDocumentationFilter.ofWithResponseFields(
//                        "adminItem-get-admin-item", // 문서 식별자 (파일 이름)
//                        new FieldDescriptor[] {
//                                fieldWithPath("success").description("요청 성공 여부")
//                                        .attributes(
//                                        key("title").value("AdminItemResponse"),  // Swagger 스키마 이름
//                                        key("tags").value("adminItem")           // Swagger 그룹 태그
//                                ),
//                                fieldWithPath("code").description("응답 코드"),
//                                fieldWithPath("httpStatus").description("HTTP 상태"),
//                                fieldWithPath("message").description("응답 메시지"),
//                                fieldWithPath("result[].id").description("상품 ID"),
//                                fieldWithPath("result[].name").description("상품 이름"),
//                                fieldWithPath("result[].imageUrl").description("상품 이미지 URL"),
//                                fieldWithPath("result[].stockQuantity").description("재고 수량"),
//                                fieldWithPath("result[].type").description("상품 종류"),
//                                fieldWithPath("result[].brand").description("브랜드"),
//                                fieldWithPath("result[].price").description("상품 가격")
//                        }
//                ))
//                .when()
//                .get("/api/admin/item") // 실제 요청 URI
//                .then()
//                .statusCode(200) // HTTP 상태 코드 검증
//                .extract().asString(); // 응답 본문 추출 (문서화 검토용)
//
//        System.out.println("응답 바디: \n" + responseBody);
//    }
//}