package com.jelly.zzirit.domain.cart.service;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;
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
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

	@Mock private CartRepository cartRepository;
	@Mock private CartItemRepository cartItemRepository;
	@Mock private MemberRepository memberRepository;
	@Mock private ItemQueryRepository itemQueryRepository;
	@Mock private ItemStockRepository itemStockRepository;
	@Mock private TimeDealItemRepository timeDealItemRepository;
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
		CartItem savedCartItem = CartItem.of(cart, item, 2);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findCartItemWithAllJoins(cart.getId(), item.getId())).willReturn(Optional.empty());
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));
		given(cartItemRepository.save(any(CartItem.class))).willReturn(savedCartItem);

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
			.id(1L)
			.item(item)
			.price(BigDecimal.valueOf(1800000))
			.timeDeal(TimeDeal.builder().discountRatio(10).build())
			.build();
		ItemStock timeDealStock = ItemStock.builder().item(item).timeDealItem(timeDealItem).quantity(10).build();
		CartItem savedCartItem = CartItem.of(cart, item, 2);

		CartItemCreateRequest request = new CartItemCreateRequest(item.getId(), 2);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findCartItemWithAllJoins(cart.getId(), item.getId())).willReturn(Optional.empty());
		given(timeDealItemRepository.findActiveByItemId(item.getId())).willReturn(Optional.of(timeDealItem));
		given(itemStockRepository.findByTimeDealItem(timeDealItem)).willReturn(Optional.of(timeDealStock));
		given(cartItemRepository.save(any(CartItem.class))).willReturn(savedCartItem);

		CartItemFetchResponse result = cartItemService.addItemToCart(memberId, request);

		assertThat(result.isTimeDeal()).isTrue();
		assertThat(result.discountedPrice()).isEqualTo(1800000);
		assertThat(result.originalPrice()).isEqualTo(2000000);
		assertThat(result.discountRatio()).isEqualTo(10);
		assertThat(result.totalPrice()).isEqualTo(1800000 * 2);
	}

	@Test
	void 장바구니_상품_품절() {
		// given
		itemStock = ItemStock.builder().item(item).quantity(0).build();
		CartItemCreateRequest request = new CartItemCreateRequest(item.getId(), 1);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findCartItemWithAllJoins(cart.getId(), item.getId())).willReturn(Optional.empty());
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		// when & then - 재고가 0이면 예외 발생
		assertThatThrownBy(() -> cartItemService.addItemToCart(memberId, request)).isInstanceOf(
			InvalidItemException.class).hasMessageContaining("장바구니 수량이 재고를 초과할 수 없습니다.");
	}

	@Test
	void 장바구니_상품_증감() {
		CartItem cartItem = CartItem.of(cart, item, 2);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));
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
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		assertThatThrownBy(() -> cartItemService.modifyQuantity(memberId, item.getId(), -1)).isInstanceOf(
			InvalidItemException.class).hasMessageContaining("장바구니 수량은 1개 이상이어야 합니다.");

		assertThatThrownBy(() -> cartItemService.modifyQuantity(memberId, item.getId(), +1)).isInstanceOf(
			InvalidItemException.class).hasMessageContaining("장바구니 수량이 재고를 초과할 수 없습니다.");
	}

	@Test
	void 장바구니_수량_변경_실패() {
		// given
		Long memberId = 1L;
		Long itemId = 100L;

		Cart cart = Cart.builder().id(1L).member(Member.builder().id(memberId).build()).build();

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), itemId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> cartItemService.modifyQuantity(memberId, itemId, 1)).isInstanceOf(
			InvalidItemException.class).hasMessage("장바구니에 해당 상품이 존재하지 않습니다.");
	}

	@Test
	void 장바구니_상품_삭제() {
		CartItem cartItem = CartItem.of(cart, item, 1);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));

		assertThatCode(() -> cartItemService.removeItemToCart(memberId, item.getId())).doesNotThrowAnyException();
		verify(cartItemRepository).delete(cartItem);
	}

	@Test
	void 장바구니_상품_선택_삭제() {
		// given
		Long memberId = 1L;
		Long cartId = 10L;
		List<Long> itemIds = List.of(1L, 2L);

		Cart cart = createCart(cartId, memberId);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findExistingItemIdsInCart(cartId, itemIds)).willReturn(itemIds);

		// when
		cartItemService.removeItemsFromCart(memberId, itemIds);

		// then
		verify(cartItemRepository).deleteAllByCartIdAndItemIdIn(cartId, itemIds);
	}

	@Test
	void 장바구니_선택_삭제_빈값() {
		// when
		cartItemService.removeItemsFromCart(1L, List.of());

		// then
		verifyNoInteractions(cartRepository, cartItemRepository);
	}

	@Test
	void 장바구니_선택_삭제_null() {
		// when
		cartItemService.removeItemsFromCart(1L, null);

		// then
		verifyNoInteractions(cartRepository, cartItemRepository);
	}

	@Test
	void 장바구니_선택_삭제_장바구니없음() {
		// given
		given(cartRepository.findByMemberId(anyLong())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> cartItemService.removeItemsFromCart(1L, List.of(1L))).isInstanceOf(
			InvalidUserException.class).hasMessage("사용자를 찾을 수 없습니다.");
	}

	@Test
	void 장바구니_선택_삭제_상품_없음_예외() {
		// given
		Long memberId = 1L;
		Long cartId = 10L;
		List<Long> itemIds = List.of(100L);

		Cart cart = createCart(cartId, memberId);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findExistingItemIdsInCart(cartId, itemIds)).willReturn(List.of());

		// when & then
		assertThatThrownBy(() -> cartItemService.removeItemsFromCart(memberId, itemIds)).isInstanceOf(
			InvalidItemException.class).hasMessage("장바구니에 해당 상품이 존재하지 않습니다.");
	}

	@Test
	void 장바구니_전체_삭제() {
		// given
		Long memberId = 1L;
		Long cartId = 10L;

		Cart cart = createCart(cartId, memberId);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

		// when
		cartItemService.removeAllItemsFromCart(memberId);

		// then
		verify(cartItemRepository).deleteByCartId(cartId);
	}

	@Test
	void 장바구니_전체_삭제_회원없음() {
		// given
		given(cartRepository.findByMemberId(anyLong())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> cartItemService.removeAllItemsFromCart(1L)).isInstanceOf(InvalidUserException.class)
			.hasMessage("사용자를 찾을 수 없습니다.");
	}

	@Test
	void 장바구니_생성() {
		// given
		Member member = Member.builder().id(memberId).build();
		Cart newCart = Cart.builder().member(member).build();
		newCart.setId(99L); // 저장된 후 ID 할당

		CartItem savedCartItem = CartItem.of(newCart, item, 1);

		CartItemCreateRequest request = new CartItemCreateRequest(item.getId(), 1);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(cartRepository.save(any(Cart.class))).willReturn(newCart);
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findCartItemWithAllJoins(newCart.getId(), item.getId())).willReturn(Optional.empty());
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));
		given(cartItemRepository.save(any(CartItem.class))).willReturn(savedCartItem);

		// when
		CartItemFetchResponse result = cartItemService.addItemToCart(memberId, request);

		// then
		assertThat(result.itemId()).isEqualTo(item.getId());
		assertThat(result.quantity()).isEqualTo(1);
		verify(cartRepository).save(any(Cart.class)); // 장바구니 저장됨
		verify(memberRepository).findById(memberId); // 회원 조회됨
		verify(cartItemRepository).save(any(CartItem.class)); // 장바구니 아이템 저장됨
	}

	@Test
	void 장바구니_상품_재고_초과_예외() {
		// given
		CartItemCreateRequest request = new CartItemCreateRequest(item.getId(), 9);
		CartItem existingCartItem = CartItem.of(cart, item, 3); // 기존 수량 있음
		itemStock = ItemStock.builder().item(item).quantity(10).build(); // 재고 10 → 총 12 > 10 → 예외

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findCartItemWithAllJoins(cart.getId(), item.getId())).willReturn(
			Optional.of(existingCartItem));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		// when & then
		assertThatThrownBy(() -> cartItemService.addItemToCart(memberId, request)).isInstanceOf(
				InvalidItemException.class)
			.hasMessageContaining(BaseResponseStatus.CART_QUANTITY_EXCEEDS_STOCK.getMessage());
	}


	@Test
	void 장바구니_상품_삭제_예외() {
		// given
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> cartItemService.removeItemToCart(memberId, item.getId())).isInstanceOf(
			InvalidItemException.class).hasMessageContaining(BaseResponseStatus.ITEM_NOT_FOUND_IN_CART.getMessage());

		verify(cartItemRepository).findByCartIdAndItemId(cart.getId(), item.getId());
	}

	@Test
	void 장바구니_조회_사용자_예외() {
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> cartItemService.removeItemToCart(memberId, item.getId())).isInstanceOf(
			InvalidUserException.class).hasMessage(USER_NOT_FOUND.getMessage());

		verify(cartRepository).findByMemberId(memberId);
	}

	@Test
	void 장바구니_상품_추가_상품_예외() {
		CartItemCreateRequest request = new CartItemCreateRequest(999L, 1);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(999L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> cartItemService.addItemToCart(memberId, request)).isInstanceOf(
			InvalidItemException.class).hasMessageContaining(BaseResponseStatus.ITEM_NOT_FOUND.getMessage());
	}

	@Test
	void 장바구니_상품_추가_재고_예외() {
		CartItemCreateRequest request = new CartItemCreateRequest(item.getId(), 1);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.empty());

		assertThatThrownBy(() -> cartItemService.addItemToCart(memberId, request)).isInstanceOf(
			InvalidItemException.class).hasMessageContaining(BaseResponseStatus.ITEM_STOCK_NOT_FOUND.getMessage());
	}

	@Test
	void 타임딜_재고_없음_예외() {
		// given
		item.changeItemStatus(ItemStatus.TIME_DEAL);
		TimeDealItem timeDealItem = TimeDealItem.builder()
			.id(1L)
			.item(item)
			.price(BigDecimal.valueOf(1800000))
			.timeDeal(TimeDeal.builder().discountRatio(10).build())
			.build();

		CartItemCreateRequest request = new CartItemCreateRequest(item.getId(), 1);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findCartItemWithAllJoins(cart.getId(), item.getId())).willReturn(Optional.empty());
		given(timeDealItemRepository.findActiveByItemId(item.getId())).willReturn(Optional.of(timeDealItem));
		given(itemStockRepository.findByTimeDealItem(timeDealItem)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> cartItemService.addItemToCart(memberId, request)).isInstanceOf(
			InvalidItemException.class).hasMessageContaining(BaseResponseStatus.ITEM_STOCK_NOT_FOUND.getMessage());
	}

	@Test
	void 일반_상품_재고_없음_예외() {
		// given
		CartItemCreateRequest request = new CartItemCreateRequest(item.getId(), 1);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemQueryRepository.findItemWithTypeJoin(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findCartItemWithAllJoins(cart.getId(), item.getId())).willReturn(Optional.empty());
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> cartItemService.addItemToCart(memberId, request)).isInstanceOf(
			InvalidItemException.class).hasMessageContaining(BaseResponseStatus.ITEM_STOCK_NOT_FOUND.getMessage());
	}

	private Cart createCart(Long cartId, Long memberId) {
		return Cart.builder().id(cartId).member(Member.builder().id(memberId).build()).build();
	}
}