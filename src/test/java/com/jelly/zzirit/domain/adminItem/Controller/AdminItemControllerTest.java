package com.jelly.zzirit.domain.adminItem.Controller;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.restdocs.snippet.Attributes.key;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;
import com.jelly.zzirit.global.support.RestDocsSupport;

// SpringBootTest 환경 설정
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// MockMvc 자동 설정
@AutoConfigureMockMvc
// 테스트용 설정 파일 사용 (application-test.yml)
@ActiveProfiles("test")
class AdminItemControllerTest extends RestDocsSupport {

    @Autowired
    private JwtUtil jwtUtil; // 테스트용 JWT 발급 유틸

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
}