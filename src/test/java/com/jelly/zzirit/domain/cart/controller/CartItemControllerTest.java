package com.jelly.zzirit.domain.cart.controller;

import static com.jelly.zzirit.domain.item.domain.fixture.BrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.ItemFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.ItemStockFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeBrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeFixture.*;
import static io.restassured.RestAssured.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;

import com.jelly.zzirit.domain.cart.dto.request.CartItemCreateRequest;
import com.jelly.zzirit.domain.cart.entity.Cart;
import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.cart.repository.CartItemRepository;
import com.jelly.zzirit.domain.cart.repository.CartRepository;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.repository.*;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.support.AcceptanceTest;
import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

class CartItemControllerTest extends AcceptanceTest {

	@Autowired private MemberRepository memberRepository;
	@Autowired private TypeRepository typeRepository;
	@Autowired private BrandRepository brandRepository;
	@Autowired private TypeBrandRepository typeBrandRepository;
	@Autowired private ItemRepository itemRepository;
	@Autowired private ItemStockRepository itemStockRepository;
	@Autowired private CartRepository cartRepository;
	@Autowired private CartItemRepository cartItemRepository;

	private Long itemId;

	private Member member;

	@BeforeEach
	void setUp() {
		member = memberRepository.save(Member.builder()
			.memberEmail("test@example.com")
			.memberName("테스트")
			.password("test1234!")
			.role(Role.ROLE_USER)
			.build());

		Type type = typeRepository.save(노트북());
		Brand brand = brandRepository.save(삼성());
		TypeBrand typeBrand = typeBrandRepository.save(타입_브랜드_생성(type, brand));
		Item item = itemRepository.save(삼성_노트북(typeBrand));
		itemStockRepository.save(풀재고_상품(item));

		// 장바구니 및 장바구니 항목 추가
		Cart cart = cartRepository.findByMemberId(member.getId())
			.orElseGet(() -> cartRepository.save(Cart.builder().member(member).build()));
		cartItemRepository.save(CartItem.of(cart, item, 2));

		itemId = item.getId();
	}

	@Test
	void 장바구니_상품_추가() {
		CartItemCreateRequest request = new CartItemCreateRequest(itemId, 2);

		given(spec)
			.contentType(APPLICATION_JSON_VALUE)
			.cookie(getCookie())
			.body(request)
			.filter(OpenApiDocumentationFilter.of(
				"장바구니 상품 추가",
				new FieldDescriptor[]{
					fieldWithPath("itemId").description("상품 ID").type(NUMBER),
					fieldWithPath("quantity").description("추가할 수량").type(NUMBER)
				},
				new FieldDescriptor[]{
					fieldWithPath("success").description("성공 여부").type(BOOLEAN),
					fieldWithPath("code").description("응답 코드").type(NUMBER),
					fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
					fieldWithPath("message").description("메시지").type(STRING),
					fieldWithPath("result.cartItemId").description("장바구니 항목 ID").type(NUMBER),
					fieldWithPath("result.itemId").description("상품 ID").type(NUMBER),
					fieldWithPath("result.itemName").description("상품 이름").type(STRING),
					fieldWithPath("result.type").description("상품 종류").type(STRING),
					fieldWithPath("result.brand").description("브랜드명").type(STRING),
					fieldWithPath("result.quantity").description("수량").type(NUMBER),
					fieldWithPath("result.imageUrl").description("이미지 URL").type(STRING),
					fieldWithPath("result.originalPrice").description("원가").type(NUMBER),
					fieldWithPath("result.discountedPrice").description("할인된 가격").type(NUMBER),
					fieldWithPath("result.totalPrice").description("총 가격").type(NUMBER),
					fieldWithPath("result.isTimeDeal").description("타임딜 여부").type(BOOLEAN),
					fieldWithPath("result.discountRatio").description("할인율").type(NUMBER).optional(),
					fieldWithPath("result.isSoldOut").description("품절 여부").type(BOOLEAN)
				}
			))
			.when()
			.post("/api/cart/items")
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	void 장바구니_상품_삭제() {
		given(spec)
			.cookie(getCookie())
			.filter(OpenApiDocumentationFilter.ofWithPathParamsAndResponseFields(
				"장바구니 상품 삭제",
				new ParameterDescriptor[]{
					parameterWithName("itemId").description("삭제할 상품 ID")
				},
				new FieldDescriptor[]{
					fieldWithPath("success").description("성공 여부").type(BOOLEAN),
					fieldWithPath("code").description("응답 코드").type(NUMBER),
					fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
					fieldWithPath("message").description("메시지").type(STRING),
					fieldWithPath("result").description("빈 응답 객체").type(OBJECT)
				}
			))
			.when()
			.delete("/api/cart/items/{itemId}", itemId)
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	void 장바구니_수량_증가() {
		given(spec)
			.cookie(getCookie())
			.filter(OpenApiDocumentationFilter.ofWithPathParamsAndResponseFields(
				"장바구니 수량 증가",
				new ParameterDescriptor[]{
					parameterWithName("itemId").description("수량을 늘릴 상품 ID")
				},
				공통응답필드()
			))
			.when()
			.post("/api/cart/items/{itemId}/increase", itemId)
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	void 장바구니_수량_감소() {
		given(spec)
			.cookie(getCookie())
			.filter(OpenApiDocumentationFilter.ofWithPathParamsAndResponseFields(
				"장바구니 수량 감소",
				new ParameterDescriptor[]{
					parameterWithName("itemId").description("수량을 줄일 상품 ID")
				},
				공통응답필드()
			))
			.when()
			.post("/api/cart/items/{itemId}/decrease", itemId)
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	void 장바구니_수량_감소_실패() {
		// 수량 1로 재설정
		Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow();
		Item item = itemRepository.findById(itemId).orElseThrow();
		cartItemRepository.deleteAll();
		cartItemRepository.save(CartItem.of(cart, item, 1));

		// 요청 및 문서화
		given(spec)
			.cookie(getCookie(member.getId()))
			.filter(OpenApiDocumentationFilter.ofWithPathParamsAndResponseFields(
				"장바구니 수량 감소 - 실패",
				new ParameterDescriptor[]{ parameterWithName("itemId").description("상품 ID") },
				new FieldDescriptor[]{
					fieldWithPath("success").description("성공 여부").type(BOOLEAN),
					fieldWithPath("code").description("응답 코드").type(NUMBER),
					fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
					fieldWithPath("message").description("에러 메시지").type(STRING),
					fieldWithPath("result").description("빈 응답 객체").type(OBJECT)
				}
			))
			.when()
			.post("/api/cart/items/{itemId}/decrease", itemId)
			.then()
			.log().all()
			.statusCode(400);
	}

	private FieldDescriptor[] 공통응답필드() {
		return new FieldDescriptor[]{
			fieldWithPath("success").description("성공 여부").type(BOOLEAN),
			fieldWithPath("code").description("응답 코드").type(NUMBER),
			fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
			fieldWithPath("message").description("메시지").type(STRING),
			fieldWithPath("result.cartItemId").description("장바구니 항목 ID").type(NUMBER),
			fieldWithPath("result.itemId").description("상품 ID").type(NUMBER),
			fieldWithPath("result.itemName").description("상품 이름").type(STRING),
			fieldWithPath("result.type").description("상품 종류").type(STRING),
			fieldWithPath("result.brand").description("브랜드명").type(STRING),
			fieldWithPath("result.quantity").description("수량").type(NUMBER),
			fieldWithPath("result.imageUrl").description("이미지 URL").type(STRING),
			fieldWithPath("result.originalPrice").description("원가").type(NUMBER),
			fieldWithPath("result.discountedPrice").description("할인된 가격").type(NUMBER),
			fieldWithPath("result.totalPrice").description("총 가격").type(NUMBER),
			fieldWithPath("result.isTimeDeal").description("타임딜 여부").type(BOOLEAN),
			fieldWithPath("result.discountRatio").description("할인율").type(NUMBER).optional(),
			fieldWithPath("result.isSoldOut").description("품절 여부").type(BOOLEAN)
		};
	}
}