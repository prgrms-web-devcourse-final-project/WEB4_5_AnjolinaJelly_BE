package com.jelly.zzirit.domain.item.contorller;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.*;
import static com.jelly.zzirit.domain.item.domain.fixture.BrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeBrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeFixture.*;
import static io.restassured.RestAssured.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.repository.BrandRepository;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;
import com.jelly.zzirit.domain.item.repository.TypeRepository;
import com.jelly.zzirit.global.support.AcceptanceRabbitTest;

import io.restassured.filter.Filter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@Disabled
public class BrandControllerRabbitTest extends AcceptanceRabbitTest {

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private TypeBrandRepository typeBrandRepository;

	@Autowired
	private TypeRepository typeRepository;

	@Test
	void 상품_종류로_브랜드_조회하면_200() {
		// when
		Type 노트북 = typeRepository.save(노트북());
		Brand 삼성 = brandRepository.save(삼성());
		Brand 애플 = brandRepository.save(브랜드_생성("애플"));
		typeBrandRepository.saveAll(List.of(
			타입_브랜드_생성(노트북, 삼성),
			타입_브랜드_생성(노트북, 애플)
		));

		RequestSpecification 요청_준비 = given(spec)
			.cookie(getCookie())
			.filter(성공_API_문서_생성());

		// when
		Response 응답 = 요청_준비.when()
			.get("/api/brands/{type-id}", 노트북.getId());

		응답.then()
			.statusCode(200);
	}

	private Filter 성공_API_문서_생성() {
		return document(
			"상품 종류로 브랜드 조회",
			resourceDetails()
				.summary("상품 종류로 브랜드 조회하기")
				.description("상품 종류 Id로 브랜드를 조회합니다."),
			responseFields(
				fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
				fieldWithPath("code").description("응답 코드").type(NUMBER),
				fieldWithPath("httpStatus").description("HTTP 상태").type(NUMBER),
				fieldWithPath("message").description("응답 메시지").type(STRING),
				fieldWithPath("result[].brandId").description("브랜드 ID").type(NUMBER),
				fieldWithPath("result[].name").description("브랜드 이름").type(STRING)
			)
		);
	}
}
