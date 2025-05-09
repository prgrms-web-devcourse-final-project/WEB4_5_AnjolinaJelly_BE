package com.jelly.zzirit.domain.cart.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.cart.dto.request.CartItemAddRequest;
import com.jelly.zzirit.domain.cart.dto.response.CartItemResponse;
import com.jelly.zzirit.domain.cart.service.CartItemService;
import com.jelly.zzirit.domain.cart.service.CartService;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;
import com.jelly.zzirit.global.support.AcceptanceTest;

import jakarta.servlet.http.Cookie;

@AutoConfigureMockMvc
@Disabled
class CartItemControllerTest extends AcceptanceTest {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CartItemService cartItemService = Mockito.mock(CartItemService.class);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@TestConfiguration
	static class TestMockConfig {
		@Bean
		public CartItemService cartItemService() {
			return Mockito.mock(CartItemService.class);
		}

		@Bean
		public CartService cartService() {
			return Mockito.mock(CartService.class);
		}

		@Bean
		public CartItemController cartItemController(CartItemService cartItemService) {
			return new CartItemController(cartItemService);
		}
	}

	@Test
	void 장바구니_상품_추가() throws Exception {
		// given
		Long memberId = 1L;
		String accessToken = jwtUtil.createJwt("access", memberId, Role.ROLE_USER, 3600);

		CartItemAddRequest request = new CartItemAddRequest();
		request.setItemId(10L);
		request.setQuantity(3);

		CartItemResponse mockResponse = new CartItemResponse(
			4L,
			10L,
			"니콘 노트북 NC-Book Ultra",
			"노트북",
			"니콘",
			3,
			"https://i.postimg.cc/C1f7fpyp/image.avif",
			1480000,
			1480000,
			4440000,
			false,
			null,
			false
		);

		given(cartItemService.addItemToCart(eq(memberId), any(CartItemAddRequest.class)))
			.willReturn(mockResponse);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/cart/items")
				.cookie(new Cookie("access", accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(document("cart-post-add-item",
				requestFields(
					fieldWithPath("itemId").description("상품 ID"),
					fieldWithPath("quantity").description("수량").attributes(key("constraints").value("최소 1 이상"))
				),
				responseFields(
					fieldWithPath("success").description("요청 성공 여부"),
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("httpStatus").description("HTTP 상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("result.cartItemId").description("장바구니 항목 ID"),
					fieldWithPath("result.itemId").description("상품 ID"),
					fieldWithPath("result.itemName").description("상품명"),
					fieldWithPath("result.type").description("상품 종류"),
					fieldWithPath("result.brand").description("브랜드명"),
					fieldWithPath("result.quantity").description("수량"),
					fieldWithPath("result.imageUrl").description("상품 이미지 URL"),
					fieldWithPath("result.originalPrice").description("정가"),
					fieldWithPath("result.discountedPrice").description("할인가"),
					fieldWithPath("result.totalPrice").description("총 가격"),
					fieldWithPath("result.timeDeal").description("타임딜 여부"),
					fieldWithPath("result.discountRatio").description("할인율").optional(),
					fieldWithPath("result.soldOut").description("품절 여부")
				)
			));
	}

	@Test
	void 장바구니_추가_실패() {
		Long userId = 1L;
		Role role = Role.ROLE_USER;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600);

		String invalidRequestBody = """
			{
				"itemId": 9,
				"quantity": 0,
				"timeDeal": true
			}
			""";

		this.spec
			.cookie("access", accessToken)
			.contentType("application/json")
			.body(invalidRequestBody)
			.filter(OpenApiDocumentationFilter.of(
				"cart-post-add-item-invalid",
				new FieldDescriptor[] {
					fieldWithPath("itemId").description("상품 ID"),
					fieldWithPath("quantity").description("0 이하 수량"),
					fieldWithPath("timeDeal").description("타임딜 여부")
						.attributes(
						key("title").value("CartItemAddRequest"),
						key("tags").value("cart")
					)
				},
				new FieldDescriptor[] {
					fieldWithPath("success").description("false"),
					fieldWithPath("code").description("에러 코드"),
					fieldWithPath("httpStatus").description("HTTP 상태"),
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("result").description("에러 객체 (비어있음)").optional()
				}
			))
			.when()
			.post("/api/cart/items")
			.then()
			.statusCode(400);
	}

	@Test
	void 장바구니_상품삭제() {
		// given
		Long userId = 1L;
		Role role = Role.ROLE_USER;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600);

		this.spec
			.cookie("access", accessToken)
			.filter(OpenApiDocumentationFilter.ofWithPathParamsAndResponseFields(
				"cart-delete-remove-item",

				new ParameterDescriptor[] {
					parameterWithName("itemId").description("삭제할 상품의 ID")
				},

				new FieldDescriptor[] {
					fieldWithPath("success").description("요청 성공 여부")
						.attributes(
						key("title").value("Empty"),
						key("tags").value("cart")
					),
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("httpStatus").description("HTTP 상태"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("result").description("빈 응답 객체 (Empty)").optional()
				}
			))
			.when()
			.delete("/api/cart/items/{itemId}", 9L)
			.then()
			.statusCode(200);
	}
}