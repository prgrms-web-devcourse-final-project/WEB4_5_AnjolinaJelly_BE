package com.jelly.zzirit.domain.cart.controller;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;

import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;
import com.jelly.zzirit.global.support.RestDocsSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CartControllerTest extends RestDocsSupport {

	@Test
	void 장바구니_조회_API_문서() {
		String responseBody = this.spec
			.filter(OpenApiDocumentationFilter.ofWithResponseFields(
				"cart-get-my-cart",
				new FieldDescriptor[]{
					fieldWithPath("success").description("요청 성공 여부"),
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("httpStatus").description("HTTP 상태"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("result.cartId").description("장바구니 ID"),
					fieldWithPath("result.items[].cartItemId").description("장바구니 항목 ID"),
					fieldWithPath("result.items[].itemId").description("상품 ID"),
					fieldWithPath("result.items[].itemName").description("상품 이름"),
					fieldWithPath("result.items[].itemImageUrl").description("상품 이미지 URL"),
					fieldWithPath("result.items[].quantity").description("수량"),
					fieldWithPath("result.items[].unitPrice").description("단가"),
					fieldWithPath("result.items[].totalPrice").description("총 가격"),
					fieldWithPath("result.items[].timeDeal").description("타임딜 여부"),
					fieldWithPath("result.items[].discountRatio").description("할인율").optional(),
					fieldWithPath("result.totalQuantity").description("전체 수량"),
					fieldWithPath("result.totalAmount").description("총 결제 금액")
				}
			))
			.when()
			.get("/api/cart/me")
			.then()
			.log().all()
			.statusCode(200)
			.extract().asString();

		System.out.println("응답 바디: \n" + responseBody);
	}
}