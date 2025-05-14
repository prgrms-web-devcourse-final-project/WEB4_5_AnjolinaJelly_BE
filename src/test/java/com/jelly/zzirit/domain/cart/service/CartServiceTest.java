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
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
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
		Type type = Type.builder().name("스마트폰").build();
		Brand brand = Brand.builder().name("Apple").build();
		sharedTypeBrand = TypeBrand.builder().type(type).brand(brand).build();

		cart = Cart.builder()
			.member(Member.builder().id(memberId).build())
			.build();
		cart.setId(100L);
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
		ci.setId(item.getId() * 10);
		return ci;
	}

	private ItemStock createStock(Item item, int quantity) {
		return ItemStock.builder().item(item).quantity(quantity).build();
	}

	private void givenCart(List<CartItem> items) {
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findAllWithItemByCartId(cart.getId())).willReturn(items);
	}

	@Test
	void 장바구니_일반() {
		Item item = createItem(10L, "iPhone", 1500000, ItemStatus.NONE);
		CartItem ci = createCartItem(item, 2);
		ItemStock stock = createStock(item, 100);

		givenCart(List.of(ci));
		given(itemStockRepository.findAllByItemIdIn(List.of(item.getId()))).willReturn(List.of(stock));

		CartFetchResponse result = cartService.getMyCart(memberId);

		assertThat(result.cartId()).isEqualTo(cart.getId());
		assertThat(result.items()).hasSize(1);
		CartItemFetchResponse res = result.items().get(0);
		assertThat(res.itemId()).isEqualTo(item.getId());
		assertThat(res.quantity()).isEqualTo(2);
		assertThat(res.totalPrice()).isEqualTo(1500000 * 2);
	}

	@Test
	void 장바구니_타임딜() {
		Item item = createItem(11L, "Z Fold", 2000000, ItemStatus.TIME_DEAL);
		CartItem ci = createCartItem(item, 2);
		ItemStock stock = createStock(item, 50);
		TimeDealItem tdi = TimeDealItem.builder()
			.item(item)
			.price(BigDecimal.valueOf(1800000))
			.timeDeal(TimeDeal.builder().discountRatio(10).build())
			.build();

		givenCart(List.of(ci));
		given(itemStockRepository.findAllByItemIdIn(List.of(item.getId()))).willReturn(List.of(stock));
		given(timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId())).willReturn(Optional.of(tdi));

		CartItemFetchResponse res = cartService.getMyCart(memberId).items().get(0);

		assertThat(res.isTimeDeal()).isTrue();
		assertThat(res.discountedPrice()).isEqualTo(1800000);
		assertThat(res.totalPrice()).isEqualTo(1800000 * 2);
	}

	@Test
	void 장바구니_품절() {
		Item item = createItem(12L, "에어팟", 500000, ItemStatus.NONE);
		CartItem ci = createCartItem(item, 1);
		ItemStock stock = createStock(item, 0);

		givenCart(List.of(ci));
		given(itemStockRepository.findAllByItemIdIn(List.of(item.getId()))).willReturn(List.of(stock));

		CartItemFetchResponse res = cartService.getMyCart(memberId).items().get(0);

		assertThat(res.isSoldOut()).isTrue();
		assertThat(res.totalPrice()).isEqualTo(500000);
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
		ItemStock s2 = createStock(timeDeal, 5);
		ItemStock s3 = createStock(soldOut, 0);

		TimeDealItem td = TimeDealItem.builder()
			.item(timeDeal)
			.price(BigDecimal.valueOf(1800000))
			.timeDeal(TimeDeal.builder().discountRatio(10).build())
			.build();

		givenCart(List.of(ci1, ci2, ci3));
		given(itemStockRepository.findAllByItemIdIn(List.of(1L, 2L, 3L)))
			.willReturn(List.of(s1, s2, s3));
		given(timeDealItemRepository.findActiveTimeDealItemByItemId(2L))
			.willReturn(Optional.of(td));

		CartFetchResponse result = cartService.getMyCart(memberId);
		assertThat(result.items()).hasSize(3);
		assertThat(result.cartTotalQuantity()).isEqualTo(3);
		assertThat(result.cartTotalPrice()).isEqualTo(1000000 + 1800000 * 2);
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
		given(itemStockRepository.findAllByItemIdIn(List.of(item.getId()))).willReturn(List.of()); // 재고 없음 → 비워서 반환

		// when & then
		assertThatThrownBy(() -> cartService.getMyCart(memberId))
			.isInstanceOf(InvalidItemException.class)
			.hasMessageContaining(BaseResponseStatus.ITEM_STOCK_NOT_FOUND.getMessage());
	}

}