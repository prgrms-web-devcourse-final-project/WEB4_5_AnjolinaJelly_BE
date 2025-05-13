package com.jelly.zzirit.domain.cart.controller;

import static com.jelly.zzirit.domain.item.domain.fixture.BrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.ItemFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.ItemStockFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeBrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeFixture.*;
import static io.restassured.RestAssured.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.FieldDescriptor;

import com.jelly.zzirit.domain.cart.entity.Cart;
import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.cart.repository.CartItemRepository;
import com.jelly.zzirit.domain.cart.repository.CartRepository;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.repository.BrandRepository;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;
import com.jelly.zzirit.domain.item.repository.TypeRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.support.AcceptanceTest;
import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;

class CartControllerTest extends AcceptanceTest {


	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private TypeRepository typeRepository;

	@Autowired
	private TypeBrandRepository typeBrandRepository;

	@Autowired
	private ItemStockRepository itemStockRepository;


	@Test
	void 장바구니_조회() {
		// given
		Member 유저 = memberRepository.save(
			Member.builder()
				.id(1L)
				.memberEmail("test@gamil.com")
				.memberName("테스트유저")
				.password("test1234!")
				.role(Role.ROLE_USER)
				.memberAddress("서울")
				.memberAddressDetail("101동")
				.build()
		);

		Cart 장바구니 = cartRepository.findByMemberId(유저.getId())
			.orElseGet(() -> cartRepository.save(Cart.builder().member(유저).build()));

		Type 노트북 = typeRepository.save(노트북());
		Brand 삼성 = brandRepository.save(삼성());
		Item 상품 = itemRepository.save(삼성_노트북(
			typeBrandRepository.save(타입_브랜드_생성(노트북, 삼성))
		));
		itemStockRepository.save(풀재고_상품(상품));

		cartItemRepository.save(
			CartItem
				.builder()
				.cart(장바구니)
				.item(상품)
				.quantity(1)
				.build()
		);

		// when & then
		given(spec)
			.cookie(getCookie())
			.filter(OpenApiDocumentationFilter.ofWithResponseFields(
				"내 장바구니 조회",
				new FieldDescriptor[] {
					fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
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
					fieldWithPath("result.items[].isTimeDeal").description("타임딜 상품 여부").type(BOOLEAN),
					fieldWithPath("result.items[].discountRatio").description("할인율").type(NUMBER).optional(),
					fieldWithPath("result.items[].isSoldOut").description("품절 여부").type(BOOLEAN),
				}
			))
			.when()
			.get("/api/cart/me")
			.then()
			.log().all()
			.statusCode(200);
	}
}