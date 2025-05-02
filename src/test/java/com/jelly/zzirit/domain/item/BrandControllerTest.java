package com.jelly.zzirit.domain.item;

import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.FieldDescriptor;

import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;
import com.jelly.zzirit.global.support.RestDocsSupport;

public class BrandControllerTest extends RestDocsSupport {

	@Autowired
	private JwtUtil jwtUtil;

	@Test
	void 상품_종류로_브랜드_조회_API() {
		Long userId = 1L;
		Role role = Role.ROLE_USER;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600);
		System.out.println("accessToken = " + accessToken);
		this.spec
			.header("Authorization", "Bearer " + accessToken)
			.filter(OpenApiDocumentationFilter.ofWithResponseFields(
				"상품 종류로 브랜드 조회",
				new FieldDescriptor[] {
					fieldWithPath("success").description("요청 성공 여부")
						.attributes(
						key("title").value("BrandResponse"),   // 스키마 이름
						key("tags").value("brand")             // Swagger 그룹
					),
					fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
					fieldWithPath("code").description("응답 코드").type(NUMBER),
					fieldWithPath("httpStatus").description("HTTP 상태").type(NUMBER),
					fieldWithPath("message").description("응답 메시지").type(STRING),
					fieldWithPath("result[].brandId").description("브랜드 ID").type(NUMBER),
					fieldWithPath("result[].name").description("브랜드 이름").type(STRING)
				}
			))
			.when()
			.get("/api/brands/{type-id}", 1L)
			.then()
			.log().all()
			.statusCode(200);
	}
}
