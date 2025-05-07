package com.jelly.zzirit.domain.order.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class ItemStockServiceTest {

	@InjectMocks
	private ItemStockService itemStockService;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private RLock rLock;

	@Mock
	private ItemStockRepository itemStockRepository;

	@Test
	void 정상적으로_재고를_차감한다() throws Exception {
		// given
		Long itemId = 1L;
		int quantity = 2;
		ItemStock stock = ItemStock.builder()
			.id(1L)
			.quantity(10)
			.soldQuantity(5)
			.build();

		given(redissonClient.getLock(anyString())).willReturn(rLock);
		given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
		given(rLock.isHeldByCurrentThread()).willReturn(true);
		given(itemStockRepository.findByItemId(itemId)).willReturn(Optional.of(stock));

		// when
		itemStockService.decrease(itemId, quantity);

		// then
		assertEquals(7, stock.getSoldQuantity());
		verify(rLock).unlock();
	}

	@Test
	void 락획득에_실패하면_예외를_던진다() throws Exception {
		// given
		given(redissonClient.getLock(anyString())).willReturn(rLock);
		given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(false);

		// when & then
		assertThrows(InvalidOrderException.class, () ->
			itemStockService.decrease(1L, 1));
	}

	@Test
	void 인터럽트_예외가_발생하면_InvalidOrderException_발생() throws Exception {
		// given
		given(redissonClient.getLock(anyString())).willReturn(rLock);
		given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
			.willThrow(new InterruptedException());

		// when & then
		assertThrows(InvalidOrderException.class, () ->
			itemStockService.decrease(1L, 1));
	}

	@Test
	void 재고가_부족하면_예외를_던진다() throws Exception {
		// given
		Long itemId = 1L;
		ItemStock stock = ItemStock.builder()
			.id(1L)
			.quantity(10)
			.soldQuantity(9)
			.build();

		given(redissonClient.getLock(anyString())).willReturn(rLock);
		given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
		given(rLock.isHeldByCurrentThread()).willReturn(true);
		given(itemStockRepository.findByItemId(itemId)).willReturn(Optional.of(stock));

		// when & then
		assertThrows(InvalidOrderException.class, () ->
			itemStockService.decrease(itemId, 2));
	}
}
