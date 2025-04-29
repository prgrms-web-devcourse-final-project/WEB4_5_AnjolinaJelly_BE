package com.jelly.jjirit.product.controller;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.resourceDetails;
import static com.epages.restdocs.apispec.Schema.schema;

import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import org.junit.jupiter.api.Test;

import com.jelly.jjirit.global.support.RestDocsSupport;


class ProductControllerTest extends RestDocsSupport {

	@Test
	void 상품_조회_API_문서() {
		this.spec
			.filter(document("상품 전체 조회 API",
				resourceDetails()
					.summary("상품 전체 조회")
					.description("상품 목록을 조회합니다.")
					.responseSchema(schema("ProductResponse")),
				responseFields(
					fieldWithPath("[].id").description("상품 ID").type(NUMBER),
					fieldWithPath("[].name").description("상품 이름").type(STRING),
					fieldWithPath("[].image").description("상품 이미지").type(STRING),
					fieldWithPath("[].stock").description("상품 재고 수량").type(NUMBER),
					fieldWithPath("[].price").description("상품 가격").type(NUMBER)
				)
			))
			.when()
			.get("/products")
			.then()
			.statusCode(200);
	}
}
