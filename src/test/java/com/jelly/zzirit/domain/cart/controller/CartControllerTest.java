package com.jelly.zzirit.domain.cart.controller;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.restdocs.snippet.Attributes.key;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;
import com.jelly.zzirit.global.support.RestDocsSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerTest extends RestDocsSupport {

	@Autowired
	private JwtUtil jwtUtil;

	@Test

	void 장바구니_조회_API_문서() {
		//  테스트용 토큰 생성
		Long userId = 1L;
		Role role = Role.ROLE_USER;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600); // access token (1시간)
		System.out.println("accessToken: " + accessToken);

		String responseBody = this.spec
			.header("Authorization", "Bearer " + accessToken)
			.filter(OpenApiDocumentationFilter.ofWithResponseFields(
				"cart-get-my-cart",
				new FieldDescriptor[] {
					fieldWithPath("success").description("요청 성공 여부")
						.attributes(
						key("title").value("CartResponse"),   // 스키마 이름
						key("tags").value("cart")             // Swagger 그룹
					),
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
			.statusCode(200)
			.extract().asString();

		System.out.println("응답 바디: \n" + responseBody);
	}

	@Test
	void 장바구니_상품추가_API_문서() {

		Long userId = 1L;
		Role role = Role.ROLE_USER;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600);

		String requestBody = """
			{
				"itemId": 9,
				"quantity": 2,
				"timeDeal": true
			}
			""";

		this.spec
			.header("Authorization", "Bearer " + accessToken)
			.contentType("application/json")
			.body(requestBody)
			.filter(OpenApiDocumentationFilter.of(
				"cart-post-add-item",
				new FieldDescriptor[] {
					fieldWithPath("itemId").description("상품 ID")
						.attributes(
						key("title").value("CartItemAddRequest"),
						key("tags").value("cart")
					),
					fieldWithPath("quantity").description("수량"),
					fieldWithPath("timeDeal").description("타임딜 여부")
				},
				new FieldDescriptor[] {
					fieldWithPath("success").description("요청 성공 여부"),
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("httpStatus").description("HTTP 상태"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("result.cartItemId").description("장바구니 항목 ID"),
					fieldWithPath("result.itemId").description("상품 ID"),
					fieldWithPath("result.itemName").description("상품 이름"),
					fieldWithPath("result.itemImageUrl").description("상품 이미지 URL"),
					fieldWithPath("result.quantity").description("수량"),
					fieldWithPath("result.unitPrice").description("단가"),
					fieldWithPath("result.totalPrice").description("총 가격"),
					fieldWithPath("result.timeDeal").description("타임딜 여부"),
					fieldWithPath("result.discountRatio").description("할인율").optional()
				}
			))
			.when()
			.post("/api/cart/items")
			.then()
			.statusCode(200);
	}

	@Test
	void 장바구니_추가_실패_잘못된_요청_문서() {
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
			.header("Authorization", "Bearer " + accessToken)
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
	void 장바구니_상품삭제_API_문서() {
		Long userId = 1L;
		Role role = Role.ROLE_USER;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600);

		this.spec
			.header("Authorization", "Bearer " + accessToken)
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