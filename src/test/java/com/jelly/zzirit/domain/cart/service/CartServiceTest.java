package com.jelly.zzirit.domain.cart.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.cart.dto.response.CartFetchResponse;
import com.jelly.zzirit.domain.cart.dto.response.CartItemFetchResponse;
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
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock private CartRepository cartRepository;
	@Mock private CartItemRepository cartItemRepository;
	@Mock private MemberRepository memberRepository;
	@Mock private TimeDealItemRepository timeDealItemRepository;
	@Mock private ItemStockRepository itemStockRepository;
	@InjectMocks private CartService cartService;

	private final Long memberId = 1L;
	private Cart cart;
	private TypeBrand sharedTypeBrand;

	@BeforeEach
	void setUp() {
		Type type = Type.builder().id(1L).name("전자기기").build();
		Brand brand = Brand.builder().id(1L).name("애플").build();
		sharedTypeBrand = TypeBrand.builder().id(1L).type(type).brand(brand).build();

		Member member = Member.builder().id(memberId).build();
		cart = Cart.builder().id(100L).member(member).build();
	}

	@Test
	void 장바구니_일반() {
		// given
		Item item = createItem(1L, "iPhone", 1500000, ItemStatus.NONE);
		CartItem cartItem = createCartItem(item, 2);
		ItemStock itemStock = createStock(item, 100);

		givenCart(List.of(cartItem));
		given(itemStockRepository.findAllByItemIdIn(anyList()))
			.willReturn(List.of(itemStock));

		// when
		CartFetchResponse result = cartService.getMyCart(memberId);

		// then
		CartItemFetchResponse res = result.items().get(0);

		assertThat(res.quantity()).isEqualTo(2);
		assertThat(res.totalPrice()).isEqualTo(1500000 * 2);
	}

	@Test
	void 장바구니_타임딜() {
		Item item = createItem(11L, "Z Fold", 2000000, ItemStatus.TIME_DEAL);
		CartItem ci = createCartItem(item, 2);
		TimeDealItem tdi = TimeDealItem.builder()
			.item(item)
			.price(BigDecimal.valueOf(1800000))
			.timeDeal(TimeDeal.builder().discountRatio(10).build())
			.build();
		ItemStock timeDealStock = createTimeDealStock(item, tdi, 50);

		givenCart(List.of(ci));
		given(timeDealItemRepository.findActiveByItemIds(List.of(item.getId())))
			.willReturn(List.of(tdi));
		given(itemStockRepository.findByTimeDealItem(tdi))
			.willReturn(Optional.of(timeDealStock));

		CartItemFetchResponse res = cartService.getMyCart(memberId).items().get(0);

		assertThat(res.isTimeDeal()).isTrue();
		assertThat(res.discountedPrice()).isEqualTo(1800000);
		assertThat(res.totalPrice()).isEqualTo(1800000 * 2);
	}

	@Test
	void 장바구니_품절() {
		Item item = createItem(12L, "에어팟", 500000, ItemStatus.NONE);
		CartItem ci = createCartItem(item, 1);

		givenCart(List.of(ci));
		given(itemStockRepository.findAllByItemIdIn(List.of(item.getId())))
			.willReturn(List.of()); // 재고 없음으로 반환
		given(timeDealItemRepository.findActiveByItemIds(anyList()))
			.willReturn(List.of());

		CartItemFetchResponse res = cartService.getMyCart(memberId).items().get(0);

		assertThat(res.isSoldOut()).isTrue(); // 품절 상태 확인
		assertThat(res.totalPrice()).isEqualTo(0); // 품절 상품은 총 가격 0
		assertThat(cartService.getMyCart(memberId).cartTotalPrice()).isEqualTo(0);
	}

	@Test
	void 장바구니_혼합() {
		Item normal = createItem(1L, "노트북", 1000000, ItemStatus.NONE);
		Item timeDeal = createItem(2L, "Z Fold", 2000000, ItemStatus.TIME_DEAL);
		Item soldOut = createItem(3L, "에어팟", 500000, ItemStatus.NONE);

		CartItem ci1 = createCartItem(normal, 1);
		CartItem ci2 = createCartItem(timeDeal, 2);
		CartItem ci3 = createCartItem(soldOut, 1);

		ItemStock s1 = createStock(normal, 10);

		TimeDealItem td = TimeDealItem.builder()
			.item(timeDeal)
			.price(BigDecimal.valueOf(1800000))
			.timeDeal(TimeDeal.builder().discountRatio(10).build())
			.build();
		ItemStock s2 = createTimeDealStock(timeDeal, td, 5);

		givenCart(List.of(ci1, ci2, ci3));
		given(itemStockRepository.findAllByItemIdIn(List.of(1L, 2L, 3L)))
			.willReturn(List.of(s1)); // soldOut 제외
		given(timeDealItemRepository.findActiveByItemIds(List.of(1L, 2L, 3L)))
			.willReturn(List.of(td));
		given(itemStockRepository.findByTimeDealItem(td))
			.willReturn(Optional.of(s2));

		CartFetchResponse result = cartService.getMyCart(memberId);
		assertThat(result.items()).hasSize(3);
		assertThat(result.cartTotalQuantity()).isEqualTo(3);
		assertThat(result.cartTotalPrice()).isEqualTo(1000000 + 1800000 * 2);
	}

	@Test
	void 타임딜상품_재고_선택_확인() {
		// given
		Long itemId = 11L;
		Item item = createItem(itemId, "Z Fold", 2000000, ItemStatus.TIME_DEAL);
		CartItem cartItem = createCartItem(item, 5); // 담은 수량: 5개

		TimeDealItem timeDealItem = TimeDealItem.builder()
			.id(1000L)
			.item(item)
			.price(BigDecimal.valueOf(1800000))
			.timeDeal(TimeDeal.builder().discountRatio(10).build())
			.build();

		ItemStock timeDealStock = createTimeDealStock(item, timeDealItem, 5);

		givenCart(List.of(cartItem));
		given(timeDealItemRepository.findActiveByItemIds(List.of(itemId)))
			.willReturn(List.of(timeDealItem));
		given(itemStockRepository.findByTimeDealItem(timeDealItem))
			.willReturn(Optional.of(timeDealStock));

		// when
		CartItemFetchResponse res = cartService.getMyCart(memberId).items().get(0);

		// then
		assertThat(res.isTimeDeal()).isTrue();               // 타임딜 상품
		assertThat(res.discountedPrice()).isEqualTo(1800000);
		assertThat(res.totalPrice()).isEqualTo(1800000 * 5); // 할인 가격 * 수량
		assertThat(res.quantity()).isEqualTo(5);             // 수량 5개 → 타임딜 재고 충분
	}

	@Test
	void 장바구니_생성() {
		// given
		Member member = Member.builder().id(memberId).build();
		Cart newCart = Cart.builder().member(member).build();
		newCart.setId(123L);

		Item item = createItem(99L, "Galaxy S24", 1200000, ItemStatus.NONE);
		CartItem cartItem = createCartItem(item, 1);
		ItemStock stock = createStock(item, 5);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(cartRepository.save(any(Cart.class))).willReturn(newCart);
		given(cartItemRepository.findAllWithItemByCartId(newCart.getId())).willReturn(List.of(cartItem));
		given(itemStockRepository.findAllByItemIdIn(List.of(item.getId()))).willReturn(List.of(stock));

		// when
		CartFetchResponse result = cartService.getMyCart(memberId);

		// then
		assertThat(result.cartId()).isEqualTo(123L);
		assertThat(result.items()).hasSize(1);
		verify(cartRepository).save(any(Cart.class));
	}

	@Test
	void 장바구니_생성_예외() {
		// given
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());
		given(memberRepository.findById(memberId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> cartService.getMyCart(memberId))
			.isInstanceOf(InvalidUserException.class)
			.hasMessageContaining(BaseResponseStatus.USER_NOT_FOUND.getMessage());

		verify(cartRepository, never()).save(any()); // 저장 시도 안 함
	}

	@Test
	void 장바구니_상품_재고_없음_예외() {
		// given
		Item item = createItem(77L, "Nothing Phone", 900000, ItemStatus.NONE);
		CartItem ci = createCartItem(item, 1);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findAllWithItemByCartId(cart.getId())).willReturn(List.of(ci));
		given(itemStockRepository.findAllByItemIdIn(List.of(item.getId()))).willReturn(List.of()); // 재고 없음
		given(timeDealItemRepository.findActiveByItemIds(anyList())).willReturn(List.of());

		// when & then
		CartItemFetchResponse res = cartService.getMyCart(memberId).items().get(0);

		assertThat(res.isSoldOut()).isTrue(); // 품절 상태로 표시
		assertThat(res.totalPrice()).isEqualTo(0); // 품절 상품은 총 가격 0
		assertThat(cartService.getMyCart(memberId).cartTotalPrice()).isEqualTo(0); // 합계에는 제외
	}

	private Item createItem(Long id, String name, int price, ItemStatus status) {
		return Item.builder()
			.id(id)
			.name(name)
			.price(BigDecimal.valueOf(price))
			.itemStatus(status)
			.imageUrl("")
			.typeBrand(sharedTypeBrand)
			.build();
	}

	private CartItem createCartItem(Item item, int quantity) {
		CartItem ci = CartItem.of(cart, item, quantity);
		ci.setId(item.getId() * 10); // 테스트 식별 목적
		return ci;
	}

	private ItemStock createStock(Item item, int quantity) {
		return ItemStock.builder()
			.item(item)
			.quantity(quantity)
			.soldQuantity(0)
			.timeDealItem(null) // 일반 상품 조건 명시
			.build();
	}

	private ItemStock createTimeDealStock(Item item, TimeDealItem timeDealItem, int quantity) {
		return ItemStock.builder()
			.item(item)
			.quantity(quantity)
			.soldQuantity(0)
			.timeDealItem(timeDealItem)
			.build();
	}

	private void givenCart(List<CartItem> items) {
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findAllWithItemByCartId(cart.getId())).willReturn(items);
	}
}