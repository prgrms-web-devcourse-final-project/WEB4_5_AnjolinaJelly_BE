package com.jelly.zzirit.domain.cart.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;

import com.jelly.zzirit.domain.cart.dto.response.CartItemResponse;
import com.jelly.zzirit.domain.cart.dto.response.CartResponse;
import com.jelly.zzirit.domain.cart.service.CartItemService;
import com.jelly.zzirit.domain.cart.service.CartService;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;
import com.jelly.zzirit.global.support.RestDocsSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(CartControllerTest.MockConfig.class)
@ActiveProfiles("test")
@Disabled
class CartControllerTest extends RestDocsSupport {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CartService cartService;

	@Autowired
	private CartItemService cartItemService;

	@TestConfiguration
	static class MockConfig {
		@Bean
		public CartService cartService() {
			return Mockito.mock(CartService.class);
		}

		@Bean
		@Primary
		public CartItemService cartItemService() {
			return Mockito.mock(CartItemService.class);
		}
	}

	@Test
	void 장바구니_조회() {
		// given
		Long userId = 1L;
		Role role = Role.ROLE_USER;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600);

		CartItemResponse item = new CartItemResponse(
			101L,
			5L,
			"iPhone 15",
			"스마트폰",
			"Apple",
			2,
			"https://dummyimage.com/iphone.jpg",
			1500000,
			1350000,
			2700000,
			true,
			10,
			false
		);

		CartResponse mockResponse = new CartResponse(
			1001L,
			List.of(item),
			2,
			2700000
		);

		when(cartService.getMyCart(userId)).thenReturn(mockResponse);

		// when & then
		this.spec
			.cookie("access", accessToken)
			.filter(OpenApiDocumentationFilter.ofWithResponseFields(
				"내 장바구니 조회",
				new FieldDescriptor[] {
					fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN)
						.attributes(key("title").value("CartResponse"), key("tags").value("cart")),
					fieldWithPath("code").description("응답 코드").type(NUMBER),
					fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
					fieldWithPath("message").description("응답 메시지").type(STRING),
					fieldWithPath("result.cartId").description("장바구니 ID").type(NUMBER),
					fieldWithPath("result.cartTotalQuantity").description("전체 수량").type(NUMBER),
					fieldWithPath("result.cartTotalPrice").description("전체 금액").type(NUMBER),
					fieldWithPath("result.items[].cartItemId").description("장바구니 항목 ID").type(NUMBER),
					fieldWithPath("result.items[].itemId").description("상품 ID").type(NUMBER),
					fieldWithPath("result.items[].itemName").description("상품명").type(STRING),
					fieldWithPath("result.items[].type").description("상품 종류").type(STRING),
					fieldWithPath("result.items[].brand").description("브랜드명").type(STRING),
					fieldWithPath("result.items[].quantity").description("수량").type(NUMBER),
					fieldWithPath("result.items[].imageUrl").description("상품 이미지 URL").type(STRING),
					fieldWithPath("result.items[].originalPrice").description("상품 정가").type(NUMBER),
					fieldWithPath("result.items[].discountedPrice").description("할인 적용된 가격").type(NUMBER),
					fieldWithPath("result.items[].totalPrice").description("총 가격").type(NUMBER),
					fieldWithPath("result.items[].timeDeal").description("타임딜 상품 여부").type(BOOLEAN),
					fieldWithPath("result.items[].discountRatio").description("할인율").type(NUMBER).optional(),
					fieldWithPath("result.items[].soldOut").description("품절 여부").type(BOOLEAN),
				}
			))
			.when()
			.get("/api/cart/me")
			.then()
			.log().all()
			.statusCode(200);
	}
}