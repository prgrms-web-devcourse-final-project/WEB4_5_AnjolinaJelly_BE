package com.jelly.zzirit.domain.cart.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.cart.dto.request.CartItemCreateRequest;
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
import com.jelly.zzirit.domain.item.repository.ItemQueryRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

	@Mock CartRepository cartRepository;
	@Mock CartItemRepository cartItemRepository;
	@Mock ItemQueryRepository itemQueryRepository;
	@Mock ItemStockRepository itemStockRepository;
	@Mock TimeDealItemRepository timeDealItemRepository;
	@InjectMocks CartItemService cartItemService;

	private final Long memberId = 1L;
	private Cart cart;
	private TypeBrand typeBrand;
	private Item item;
	private ItemStock itemStock;

	@BeforeEach
	void setUp() {
		Type type = Type.builder().name("노트북").build();
		Brand brand = Brand.builder().name("Apple").build();
		typeBrand = TypeBrand.builder().type(type).brand(brand).build();

		item = Item.builder()
			.id(10L)
			.name("MacBook Pro")
			.price(BigDecimal.valueOf(2000000))
			.imageUrl("url")
			.itemStatus(ItemStatus.NONE)
			.typeBrand(typeBrand)
			.build();

		itemStock = ItemStock.builder().item(item).quantity(10).build();

		cart = Cart.builder().member(Member.builder().id(memberId).build()).build();
		cart.setId(1L);
	}

	@Test
	void 장바구니_상품_추가() {
		CartItemCreateRequest request = new CartItemCreateRequest(item.getId(), 2);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.empty());
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		CartItemFetchResponse result = cartItemService.addItemToCart(memberId, request);

		assertThat(result.itemId()).isEqualTo(item.getId());
		assertThat(result.quantity()).isEqualTo(2);
		assertThat(result.isSoldOut()).isFalse();
		assertThat(result.discountedPrice()).isEqualTo(2000000);
		assertThat(result.totalPrice()).isEqualTo(2000000 * 2);
	}

	@Test
	void 장바구니_상품_할인() {
		item.changeItemStatus(ItemStatus.TIME_DEAL);
		TimeDealItem timeDealItem = TimeDealItem.builder()
			.item(item)
			.price(BigDecimal.valueOf(1800000))
			.timeDeal(TimeDeal.builder().discountRatio(10).build())
			.build();

		CartItemCreateRequest request = new CartItemCreateRequest(item.getId(), 2);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.empty());
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));
		given(timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId())).willReturn(Optional.of(timeDealItem));

		CartItemFetchResponse result = cartItemService.addItemToCart(memberId, request);

		assertThat(result.isTimeDeal()).isTrue();
		assertThat(result.discountedPrice()).isEqualTo(1800000);
		assertThat(result.originalPrice()).isEqualTo(2000000);
		assertThat(result.discountRatio()).isEqualTo(10);
		assertThat(result.totalPrice()).isEqualTo(1800000 * 2);
	}

	@Test
	void 장바구니_상품_품절() {
		itemStock = ItemStock.builder().item(item).quantity(0).build();
		CartItemCreateRequest request = new CartItemCreateRequest(item.getId(), 1);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.empty());
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		CartItemFetchResponse result = cartItemService.addItemToCart(memberId, request);

		assertThat(result.isSoldOut()).isTrue();
		assertThat(result.totalPrice()).isEqualTo(item.getPrice().intValue());
	}

	@Test
	void 장바구니_상품_증감() {
		CartItem cartItem = CartItem.of(cart, item, 2);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findWithItemJoinByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		CartItemFetchResponse increased = cartItemService.modifyQuantity(memberId, item.getId(), +1);
		assertThat(increased.quantity()).isEqualTo(3);

		CartItemFetchResponse decreased = cartItemService.modifyQuantity(memberId, item.getId(), -2);
		assertThat(decreased.quantity()).isEqualTo(1);
	}

	@Test
	void 장바구니_상품_수량_예외() {
		CartItem cartItem = CartItem.of(cart, item, 1);
		itemStock = ItemStock.builder().item(item).quantity(1).build();

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findWithItemJoinByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		assertThatThrownBy(() -> cartItemService.modifyQuantity(memberId, item.getId(), -1))
			.isInstanceOf(InvalidItemException.class)
			.hasMessageContaining("장바구니 수량은 1개 이상");

		assertThatThrownBy(() -> cartItemService.modifyQuantity(memberId, item.getId(), +1))
			.isInstanceOf(InvalidItemException.class)
			.hasMessageContaining("장바구니 수량이 재고를 초과");
	}

	@Test
	void 장바구니_상품_삭제() {
		CartItem cartItem = CartItem.of(cart, item, 1);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));

		assertThatCode(() -> cartItemService.removeItemToCart(memberId, item.getId())).doesNotThrowAnyException();
		verify(cartItemRepository).delete(cartItem);
	}
}