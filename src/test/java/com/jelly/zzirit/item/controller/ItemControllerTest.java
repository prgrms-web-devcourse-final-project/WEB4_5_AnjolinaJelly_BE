package com.jelly.zzirit.item.controller;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.Schema.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.jelly.zzirit.global.support.RestDocsSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestDocs
@ActiveProfiles("test")
class ItemControllerTest extends RestDocsSupport {

	@Test
	void 상품_조회_API_문서() {
		this.spec
			.filter(document("상품 전체 조회 API",
				resourceDetails()
					.summary("상품 전체 조회")
					.description("상품 목록을 조회합니다.")
					.responseSchema(schema("ItemResponse")),
				responseFields(
					fieldWithPath("[].id").description("상품 ID").type(NUMBER),
					fieldWithPath("[].name").description("상품 이름").type(STRING),
					fieldWithPath("[].image").description("상품 이미지").type(STRING),
					fieldWithPath("[].stock").description("상품 재고 수량").type(NUMBER),
					fieldWithPath("[].price").description("상품 가격").type(NUMBER)
				)
			))
			.when()
			.get("/items")
			.then()
			.statusCode(200);
	}
}