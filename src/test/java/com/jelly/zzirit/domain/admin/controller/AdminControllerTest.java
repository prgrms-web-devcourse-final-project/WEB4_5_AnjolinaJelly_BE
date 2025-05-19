package com.jelly.zzirit.domain.admin.controller;

import static io.restassured.RestAssured.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import com.jelly.zzirit.domain.admin.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.admin.dto.request.ItemUpdateRequest;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.repository.BrandRepository;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;
import com.jelly.zzirit.domain.item.repository.TypeRepository;
import com.jelly.zzirit.global.support.AcceptanceTest;
import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.math.BigDecimal;

public class AdminControllerTest extends AcceptanceTest {

    @Autowired private TypeRepository typeRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired
    private TypeBrandRepository typeBrandRepository;

    private Long typeId;
    private Long brandId;

    @BeforeEach
    void setUp() {
        // ✅ 1. 타입과 브랜드 및 타입-브랜드 관계 삽입
        Type type = typeRepository.save(new Type("노트북"));
        Brand brand = brandRepository.save(new Brand("삼성"));
        TypeBrand typeBrand = typeBrandRepository.save(new TypeBrand(type, brand));
        typeId = type.getId();
        brandId = brand.getId();

        // ✅ 2. 테스트용 상품 등록
        ItemCreateRequest request = new ItemCreateRequest(
                "테스트상품",
                100,
                new BigDecimal("9900"),
                typeId,
                brandId,
                "https://example.com/image.jpg"
        );

        given(spec)
                .cookie(getAdminCookie())
                .contentType(APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/admin/items")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 성공")
    void 관리자_상품_목록_조회_성공() {
        RequestSpecification 요청 = given(spec)
                .cookie(getAdminCookie())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .filter(OpenApiDocumentationFilter.ofWithPathParamsAndResponseFields(
                        "관리자 상품 목록 조회",
                        new ParameterDescriptor[] {
                                parameterWithName("name").description("상품명 (optional)").optional(),
                                parameterWithName("sort").description("정렬 기준(desc/asc)").optional(),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        },
                        new FieldDescriptor[] {
                                fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
                                fieldWithPath("code").description("응답 코드").type(NUMBER),
                                fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
                                fieldWithPath("message").description("응답 메시지").type(STRING),
                                fieldWithPath("result.content[].id").description("상품 ID").type(NUMBER),
                                fieldWithPath("result.content[].name").description("상품 이름").type(STRING),
                                fieldWithPath("result.content[].imageUrl").description("상품 이미지").type(STRING),
                                fieldWithPath("result.content[].type").description("상품 타입").type(STRING),
                                fieldWithPath("result.content[].brand").description("상품 브랜드").type(STRING),
                                fieldWithPath("result.content[].price").description("상품 가격").type(NUMBER),
                                fieldWithPath("result.content[].stockQuantity").description("재고 수량").type(NUMBER),
                                fieldWithPath("result.pageNumber").description("현재 페이지").type(NUMBER),
                                fieldWithPath("result.pageSize").description("페이지 크기").type(NUMBER),
                                fieldWithPath("result.totalElements").description("총 요소 수").type(NUMBER),
                                fieldWithPath("result.totalPages").description("총 페이지 수").type(NUMBER),
                                fieldWithPath("result.last").description("마지막 페이지 여부").type(BOOLEAN)
                        }
                ));

        Response 응답 = 요청.when()
                .get("/api/admin/items");

        응답.then()
                .statusCode(200);
    }

    @Test
    void 관리자_상품_등록() {
        // given
        ItemCreateRequest request = new ItemCreateRequest(
                "관리자테스트상품",
                50,
                new BigDecimal("15900"),
                1L,
                1L,
                "https://example.com/image.png"
        );

        Cookie adminCookie = getAdminCookie();

        // when & then
        given(spec)
                .cookie(adminCookie)
                .contentType(APPLICATION_JSON_VALUE)
                .body(request)
                .filter(OpenApiDocumentationFilter.ofWithRequestFieldsAndResponseFields(
                        "관리자 상품 등록",
                        requestFields(
                                fieldWithPath("name").description("상품 이름").type(STRING),
                                fieldWithPath("stockQuantity").description("재고 수량").type(NUMBER),
                                fieldWithPath("price").description("상품 가격").type(NUMBER),
                                fieldWithPath("typeId").description("상품 타입 ID").type(NUMBER),
                                fieldWithPath("brandId").description("브랜드 ID").type(NUMBER),
                                fieldWithPath("imageUrl").description("상품 이미지 URL").type(STRING)
                        ),
                        responseFields(
                                fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
                                fieldWithPath("code").description("응답 코드").type(NUMBER),
                                fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
                                fieldWithPath("message").description("응답 메시지").type(STRING),
                                fieldWithPath("result").description("빈 응답 객체").type(OBJECT)
                        )
                ))
                .when()
                .post("/api/admin/items")
                .then()
                .log().all()
                .statusCode(200);
    }
}