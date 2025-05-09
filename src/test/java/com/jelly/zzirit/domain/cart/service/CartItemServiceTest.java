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

import com.jelly.zzirit.domain.cart.dto.request.CartItemAddRequest;
import com.jelly.zzirit.domain.cart.dto.response.CartItemResponse;
import com.jelly.zzirit.domain.cart.dto.response.CartResponse;
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
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

	@Mock
	private CartRepository cartRepository;
	@Mock
	private CartItemRepository cartItemRepository;
	@Mock
	private ItemRepository itemRepository;
	@Mock
	private TimeDealItemRepository timeDealItemRepository;
	@Mock
	private ItemStockRepository itemStockRepository;
	@Mock
	private CartService cartService;

	@InjectMocks
	private CartItemService cartItemService;

	private final Long memberId = 1L;

	private Cart cart;
	private Item item;
	private CartItem cartItem;
	private ItemStock itemStock;

	@BeforeEach
	void setUp() {
		// 아이템 종류와 브랜드 정보 생성
		Type type = Type.builder().name("노트북").build();
		Brand brand = Brand.builder().name("Apple").build();
		TypeBrand typeBrand = TypeBrand.builder()
			.type(type)
			.brand(brand)
			.build();

		// 테스트용 상품
		item = Item.builder()
			.id(10L)
			.name("MacBook Pro")
			.price(BigDecimal.valueOf(2000000))
			.imageUrl("https://dummyimage.com/macbook.jpg")
			.itemStatus(ItemStatus.NONE)
			.typeBrand(typeBrand)
			.build();

		// 상품 재고
		itemStock = ItemStock.builder()
			.item(item)
			.quantity(10)
			.build();

		// 장바구니 생성
		cart = Cart.builder()
			.member(Member.builder().id(memberId).build())
			.build();
		cart.setId(1L);

		// 장바구니 항목
		cartItem = CartItem.of(cart, item, 1);
		cartItem.setId(100L);
	}

	@Test
	@DisplayName("상품 장바구니 추가")
	void addNewItem() {
		// given: 상품 ID와 수량이 담긴 요청 객체를 생성하고,
		// 장바구니, 상품, 재고가 모두 정상적으로 존재하는 상황을 가정
		CartItemAddRequest request = new CartItemAddRequest();
		request.setItemId(item.getId());
		request.setQuantity(2);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart)); // 회원의 장바구니 존재
		given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));   // 상품 존재
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.empty()); // 장바구니에 해당 상품 없음
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock)); // 재고 존재

		// when: 장바구니 서비스에 상품 추가 요청을 수행
		CartItemResponse response = cartItemService.addItemToCart(memberId, request);

		// then: 응답 결과가 요청한 상품 ID와 수량을 반영하고, 할인 없이 정가가 적용되며 품절 아님을 검증
		assertThat(response.getItemId()).isEqualTo(item.getId());
		assertThat(response.getQuantity()).isEqualTo(2);
		assertThat(response.getDiscountedPrice()).isEqualTo(2000000); // 정가
		assertThat(response.getTotalPrice()).isEqualTo(2000000 * 2);  // 총 가격
		assertThat(response.isSoldOut()).isFalse();                   // 품절 아님
	}

	@Test
	@DisplayName("타임딜 상품 할인 적용")
	void addTimeDealItem() {
		// given: 타임딜 상태 상품과 타임딜 정보, 요청, 재고 등 모든 mock 구성
		item.changeItemStatus(ItemStatus.TIME_DEAL);

		TimeDealItem timeDealItem = TimeDealItem.builder()
			.item(item)
			.price(BigDecimal.valueOf(1800000)) // 할인 가격
			.timeDeal(TimeDeal.builder().discountRatio(10).build()) // 할인율
			.build();

		CartItemAddRequest request = new CartItemAddRequest();
		request.setItemId(item.getId());
		request.setQuantity(2);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.empty());
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));
		given(timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId())).willReturn(Optional.of(timeDealItem));

		// when: 타임딜 상품 장바구니 추가
		CartItemResponse response = cartItemService.addItemToCart(memberId, request);

		// then: 할인 정보가 정확히 반영되어 응답되는지 검증
		assertThat(response.isTimeDeal()).isTrue();
		assertThat(response.getDiscountedPrice()).isEqualTo(1800000);
		assertThat(response.getOriginalPrice()).isEqualTo(2000000);
		assertThat(response.getDiscountRatio()).isEqualTo(10);
		assertThat(response.getTotalPrice()).isEqualTo(1800000 * 2);
	}

	@Test
	@DisplayName("품절 상품 장바구니 추가")
	void addSoldOutItem() {
		// given: 재고가 0인 상품과 장바구니 요청 객체, mock 리턴 설정
		ItemStock soldOutStock = ItemStock.builder()
			.item(item)
			.quantity(0)
			.build();

		CartItemAddRequest request = new CartItemAddRequest();
		request.setItemId(item.getId());
		request.setQuantity(1);

		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.empty());
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(soldOutStock));

		// when: 품절 상품을 장바구니에 추가
		CartItemResponse response = cartItemService.addItemToCart(memberId, request);

		// then: 품절 여부가 true이고 가격 정보는 그대로 반영되었는지 검증
		assertThat(response.getItemId()).isEqualTo(item.getId());
		assertThat(response.getQuantity()).isEqualTo(1);
		assertThat(response.isSoldOut()).isTrue();
		assertThat(response.getDiscountedPrice()).isEqualTo(2000000); // 정가
		assertThat(response.getTotalPrice()).isEqualTo(2000000); // 정가 * 수량
	}

	@Test
	@DisplayName("장바구니 상품 삭제")
	void removeItem() {
		// given: 장바구니와 해당 상품이 존재하는 상황을 가정
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));

		// when: 상품 삭제 메서드 실행
		// then: 예외 없이 실행되고, 실제로 삭제 메서드가 호출되었는지 검증
		assertThatCode(() -> cartItemService.removeItemToCart(memberId, item.getId()))
			.doesNotThrowAnyException();

		verify(cartItemRepository).delete(cartItem);
	}


	@Test
	@DisplayName("장바구니 상품 수량 증가")
	void increaseItemQuantity() {
		// given: 장바구니에 해당 상품이 1개 담겨 있는 상태
		cartItem.setQuantity(1);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		// when: 동일 상품 2개를 addItemToCart로 재추가
		CartItemAddRequest request = new CartItemAddRequest();
		request.setItemId(item.getId());
		request.setQuantity(2);
		CartItemResponse responseViaAdd = cartItemService.addItemToCart(memberId, request);

		// then: 기존 수량 1 + 추가 2 = 총 3이 반영되어야 함
		assertThat(responseViaAdd.getQuantity()).isEqualTo(3);
		assertThat(responseViaAdd.getTotalPrice()).isEqualTo(responseViaAdd.getDiscountedPrice() * 3);
		assertThat(responseViaAdd.isSoldOut()).isFalse();
		verify(cartItemRepository, never()).save(any()); // 기존 항목에 수량만 증가했으므로 save 호출되지 않음

		// when: modifyQuantity를 통해 수량을 +1 증가
		CartItemResponse responseViaModify = cartItemService.modifyQuantity(memberId, item.getId(), +1);

		// then: 총 수량 4가 반영되어야 함
		assertThat(responseViaModify.getQuantity()).isEqualTo(4);
		assertThat(responseViaModify.getTotalPrice()).isEqualTo(responseViaModify.getDiscountedPrice() * 4);
	}

	@Test
	@DisplayName("장바구니 수량 감소")
	void decreaseItemQuantity() {
		// given: 초기 수량이 3인 상태에서 수량 감소 요청
		cartItem.setQuantity(3);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		// when: 수량 1 감소 요청
		CartItemResponse result = cartItemService.modifyQuantity(memberId, item.getId(), -1);

		// then: 수량이 2로 줄고 총 가격이 할인 가격 * 2로 계산되는지 검증
		assertThat(cartItem.getQuantity()).isEqualTo(2);
		assertThat(result).isNotNull();
		assertThat(result.getQuantity()).isEqualTo(2);
		assertThat(result.getTotalPrice()).isEqualTo(result.getDiscountedPrice() * 2);
	}

	@Test
	@DisplayName("장바구니 수량 0 예외")
	void decreaseToZero_throws() {
		// given: 수량이 1인 상품이 장바구니에 담긴 상태
		cartItem.setQuantity(1);
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));

		// when / then: 수량 -1 요청 시 예외 발생 확인
		assertThatThrownBy(() -> cartItemService.modifyQuantity(memberId, item.getId(), -1))
			.isInstanceOf(InvalidItemException.class)
			.hasMessageContaining("장바구니 수량은 1개 이상이어야 합니다.");
	}

	@Test
	@DisplayName("재고 초과 예외")
	void increaseOverStock_throws() {
		// given: 현재 수량과 재고가 동일한 상태에서 수량을 추가하려는 요청
		cartItem.setQuantity(10);
		itemStock = ItemStock.builder().item(item).quantity(10).build(); // 재고 = 10
		given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
		given(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).willReturn(Optional.of(cartItem));
		given(itemStockRepository.findByItemId(item.getId())).willReturn(Optional.of(itemStock));

		// when / then: 수량 +1 요청 시 재고 초과로 예외 발생 확인
		assertThatThrownBy(() -> cartItemService.modifyQuantity(memberId, item.getId(), +1))
			.isInstanceOf(InvalidItemException.class)
			.hasMessageContaining("장바구니 수량이 재고를 초과할 수 없습니다.");
	}
}