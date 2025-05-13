package com.jelly.zzirit.domain.cart.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.cart.dto.response.CartItemFetchResponse;
import com.jelly.zzirit.domain.cart.dto.response.CartFetchResponse;
import com.jelly.zzirit.domain.cart.entity.Cart;
import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.cart.repository.CartItemRepository;
import com.jelly.zzirit.domain.cart.repository.CartRepository;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.member.entity.Member;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock
	private CartRepository cartRepository;

	@Mock
	private CartItemRepository cartItemRepository;

	@Mock
	private TimeDealItemRepository timeDealItemRepository;

	@Mock
	private ItemStockRepository itemStockRepository;

	@InjectMocks
	private CartService cartService;

	private final Long memberId = 1L;
	private Cart cart;
	private Item item;
	private CartItem cartItem;
	private ItemStock itemStock;

	@BeforeEach
	void setUp() {
		// 아이템 종류와 브랜드 정보 생성
		Type type = Type.builder().name("스마트폰").build();
		Brand brand = Brand.builder().name("Apple").build();
		TypeBrand typeBrand = TypeBrand.builder()
			.type(type)
			.brand(brand)
			.build();

		// 테스트용 상품
		item = Item.builder()
			.id(10L)
			.name("iPhone 15 Pro")
			.price(BigDecimal.valueOf(1500000)) // 150만원
			.itemStatus(ItemStatus.NONE)        // 타임딜 아님
			.imageUrl("https://dummyimage.com/iphone.jpg")
			.typeBrand(typeBrand)
			.build();

		// 재고
		itemStock = ItemStock.builder()
			.item(item)
			.quantity(100)
			.build();

		// 테스트용 회원과 장바구니 객체 생성
		cart = Cart.builder()
			.member(Member.builder().id(memberId).build())
			.build();
		cart.setId(100L); // ID는 테스트용으로 수동 설정

		// 장바구니에 iPhone 15 Pro를 2개 담은 항목 생성
		cartItem = CartItem.of(cart, item, 2);
		cartItem.setId(101L);
	}

	@Test
	@DisplayName("장바구니 상품 조회")
	void getCartWithNormalItem() {
		// given: 장바구니에 일반 상품이 담겨 있고 재고가 충분한 상태
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findAllByCartId(cart.getId())).willReturn(List.of(cartItem));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		// when: 장바구니 조회 요청
		CartFetchResponse result = cartService.getMyCart(memberId);

		// then: 상품 수량, 가격, ID, 총합 정보가 정확히 응답되는지 검증
		assertThat(result).isNotNull();
		assertThat(result.cartId()).isEqualTo(cart.getId());
		assertThat(result.items()).hasSize(1);
		CartItemFetchResponse response = result.items().get(0);
		assertThat(response.itemId()).isEqualTo(item.getId());
		assertThat(response.quantity()).isEqualTo(2);
		assertThat(result.cartTotalQuantity()).isEqualTo(2);
		assertThat(result.cartTotalPrice()).isEqualTo(1500000 * 2);
	}

	@Test
	@DisplayName("타임딜 상품 장바구니 조회")
	void getCartWithTimeDealItem() {
		// given: 타임딜 상태의 상품과 해당 타임딜 정보를 설정
		item.changeItemStatus(ItemStatus.TIME_DEAL);

		TimeDealItem timeDealItem = TimeDealItem.builder()
			.item(item)
			.price(BigDecimal.valueOf(1350000)) // 할인 가격
			.timeDeal(TimeDeal.builder().discountRatio(10).build()) // 할인율
			.build();

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findAllByCartId(cart.getId())).willReturn(List.of(cartItem));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));
		given(timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId())).willReturn(Optional.of(timeDealItem));

		// when: 장바구니 조회
		CartFetchResponse result = cartService.getMyCart(memberId);

		// then: 할인 정보가 정확히 반영되어 있는지 검증
		CartItemFetchResponse response = result.items().get(0);
		assertThat(response.originalPrice()).isEqualTo(1500000);
		assertThat(response.discountedPrice()).isEqualTo(1350000);
		assertThat(response.isTimeDeal()).isTrue();
		assertThat(response.discountRatio()).isEqualTo(10);
		assertThat(response.totalPrice()).isEqualTo(1350000 * 2);
	}

	@Test
	@DisplayName("품절 상품 장바구니 조회")
	void getCartWithSoldOutItem() {
		// given: 재고 수량이 0인 품절 상품 설정
		itemStock = ItemStock.builder()
			.item(item)
			.quantity(0)
			.build();

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findAllByCartId(cart.getId())).willReturn(List.of(cartItem));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		// when: 장바구니 조회
		CartFetchResponse result = cartService.getMyCart(memberId);

		// then: 품절 상품의 표시 여부와 전체 합산 제외 여부 검증
		CartItemFetchResponse response = result.items().get(0);

		assertThat(response.itemId()).isEqualTo(item.getId());
		assertThat(response.quantity()).isEqualTo(cartItem.getQuantity());
		assertThat(response.isSoldOut()).isTrue(); // 품절 여부
		assertThat(response.discountedPrice()).isEqualTo(item.getPrice().intValue());
		assertThat(response.totalPrice()).isEqualTo(item.getPrice().intValue() * cartItem.getQuantity());

		assertThat(result.cartTotalQuantity()).isEqualTo(0); // 품절 제외
		assertThat(result.cartTotalPrice()).isEqualTo(0);    // 품절 제외
	}

	@Test
	@DisplayName("혼합 상품 장바구니 조회")
	void getCartWithMixedItems() {
		// given: 일반 상품, 타임딜 상품, 품절 상품 각각 생성 및 mock 설정

		// 일반 상품
		Item normalItem = Item.builder()
			.id(1L)
			.name("노트북")
			.price(BigDecimal.valueOf(1000000))
			.itemStatus(ItemStatus.NONE)
			.imageUrl("url")
			.typeBrand(item.getTypeBrand())
			.build();
		CartItem normalCartItem = CartItem.of(cart, normalItem, 1);
		ItemStock normalStock = ItemStock.builder().item(normalItem).quantity(10).build();

		// 타임딜 상품
		Item timeDealItem = Item.builder()
			.id(2L)
			.name("갤럭시 Z Fold")
			.price(BigDecimal.valueOf(2000000))
			.itemStatus(ItemStatus.TIME_DEAL)
			.imageUrl("url")
			.typeBrand(item.getTypeBrand())
			.build();
		CartItem timeDealCartItem = CartItem.of(cart, timeDealItem, 2);
		ItemStock timeDealStock = ItemStock.builder().item(timeDealItem).quantity(5).build();
		TimeDealItem timeDeal = TimeDealItem.builder()
			.item(timeDealItem)
			.price(BigDecimal.valueOf(1800000))
			.timeDeal(TimeDeal.builder().discountRatio(10).build())
			.build();

		// 품절 상품
		Item soldOutItem = Item.builder()
			.id(3L)
			.name("에어팟")
			.price(BigDecimal.valueOf(500000))
			.itemStatus(ItemStatus.NONE)
			.imageUrl("url")
			.typeBrand(item.getTypeBrand())
			.build();
		CartItem soldOutCartItem = CartItem.of(cart, soldOutItem, 1);
		ItemStock soldOutStock = ItemStock.builder().item(soldOutItem).quantity(0).build();

		// Mock 설정
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findAllByCartId(cart.getId()))
			.willReturn(List.of(normalCartItem, timeDealCartItem, soldOutCartItem));
		given(itemStockRepository.findByItemId(normalItem.getId())).willReturn(Optional.of(normalStock));
		given(itemStockRepository.findByItemId(timeDealItem.getId())).willReturn(Optional.of(timeDealStock));
		given(itemStockRepository.findByItemId(soldOutItem.getId())).willReturn(Optional.of(soldOutStock));
		given(timeDealItemRepository.findActiveTimeDealItemByItemId(timeDealItem.getId())).willReturn(Optional.of(timeDeal));

		// when: 장바구니 조회
		CartFetchResponse result = cartService.getMyCart(memberId);

		// then: 항목 개수 확인
		assertThat(result).isNotNull();
		assertThat(result.items()).hasSize(3);

		// 일반 상품 검증
		CartItemFetchResponse normalRes = result.items().stream()
			.filter(r -> r.itemId().equals(normalItem.getId()))
			.findFirst().orElseThrow();
		assertThat(normalRes.isTimeDeal()).isFalse();
		assertThat(normalRes.isSoldOut()).isFalse();
		assertThat(normalRes.totalPrice()).isEqualTo(1000000);

		// 타임딜 상품 검증
		CartItemFetchResponse timeDealRes = result.items().stream()
			.filter(r -> r.itemId().equals(timeDealItem.getId()))
			.findFirst().orElseThrow();
		assertThat(timeDealRes.isTimeDeal()).isTrue();
		assertThat(timeDealRes.isSoldOut()).isFalse();
		assertThat(timeDealRes.discountedPrice()).isEqualTo(1800000);
		assertThat(timeDealRes.totalPrice()).isEqualTo(1800000 * 2);

		// 품절 상품 검증
		CartItemFetchResponse soldOutRes = result.items().stream()
			.filter(r -> r.itemId().equals(soldOutItem.getId()))
			.findFirst().orElseThrow();
		assertThat(soldOutRes.isSoldOut()).isTrue();
		assertThat(soldOutRes.totalPrice()).isEqualTo(500000);

		// 총합 검증 (품절 상품 제외)
		int expectedQuantity = 1 + 2;
		int expectedPrice = 1000000 + 1800000 * 2;
		assertThat(result.cartTotalQuantity()).isEqualTo(expectedQuantity);
		assertThat(result.cartTotalPrice()).isEqualTo(expectedPrice);
	}

}