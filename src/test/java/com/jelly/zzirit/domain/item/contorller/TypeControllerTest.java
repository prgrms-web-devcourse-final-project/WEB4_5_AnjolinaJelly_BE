package com.jelly.zzirit.domain.item.contorller;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeFixture.*;
import static io.restassured.RestAssured.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;

import com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper;
import com.jelly.zzirit.domain.item.repository.TypeRepository;
import com.jelly.zzirit.global.support.AcceptanceTest;
import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;

import io.restassured.filter.Filter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@Disabled
public class TypeControllerTest extends AcceptanceTest {

	@Autowired
	private TypeRepository typeRepository;

	@Test
	void 상품_종류_조회하면_200() {
		// given
		typeRepository.save(노트북());
		typeRepository.save(스마트폰());

		RequestSpecification 요청_준비 = given(spec)
			.cookie(getCookie())
			.filter(성공_API_문서_생성());

		// when
		Response 응답 = 요청_준비.when()
			.get("/api/types");

		// then
		응답.then()
			.statusCode(200);
	}

	private Filter 성공_API_문서_생성() {
		return document(
			"상품 종류 전체 조회",
			resourceDetails()
				.summary("상품 종류 전체 조회하기")
				.description("상품 종류 전체를 조회합니다."),
			responseFields(
				fieldWithPath("success").description("요청 성공 여부"),
				fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
				fieldWithPath("code").description("응답 코드").type(NUMBER),
				fieldWithPath("httpStatus").description("HTTP 상태").type(NUMBER),
				fieldWithPath("message").description("응답 메시지").type(STRING),
				fieldWithPath("result[].typeId").description("종류 ID").type(NUMBER),
				fieldWithPath("result[].name").description("종류 이름").type(STRING)
			)
		);
	}

}
