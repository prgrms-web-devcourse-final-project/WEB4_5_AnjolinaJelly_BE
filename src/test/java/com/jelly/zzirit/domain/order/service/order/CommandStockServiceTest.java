package com.jelly.zzirit.domain.order.service.order;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import com.jelly.zzirit.global.redis.TestContainerConfig;
import com.jelly.zzirit.global.redis.TestRedisTemplateConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisTemplateConfig.class)
@Commit
class CommandStockServiceTest extends TestContainerConfig {

	@Autowired
	private CommandStockService commandStockService;

	@Autowired
	private ItemStockRepository itemStockRepository;

	private final Long ITEM_ID = 1L;

	@Test
	@Transactional
	void 재고가_충분할_때_decrease_정상작동() {
		// when
		commandStockService.decrease(ITEM_ID, 3);

		// then
		ItemStock stock = itemStockRepository.findByItemId(ITEM_ID)
			.orElseThrow();
		assertEquals(3, stock.getSoldQuantity());
		assertEquals(7, stock.getQuantity());
	}

	@Test
	@Transactional
	void 재고가_부족할_때_decrease_실패() {
		// when & then
		InvalidOrderException ex = assertThrows(InvalidOrderException.class, () ->
			commandStockService.decrease(ITEM_ID, 999)
		);
		assertEquals(BaseResponseStatus.STOCK_REDUCE_FAILED, ex.getStatus());
	}

	@Test
	@Transactional
	void restore_정상작동() {
		// given: 선차감
		commandStockService.decrease(ITEM_ID, 5);

		// when: 복원
		commandStockService.restore(ITEM_ID, 2);

		// then
		ItemStock stock = itemStockRepository.findByItemId(ITEM_ID)
			.orElseThrow();
		assertEquals(3, stock.getSoldQuantity()); // 5 - 2
		assertEquals(7, stock.getQuantity());     // 10 - 5 + 2
	}

	@Test
	@Transactional
	void 존재하지_않는_재고_restore_실패() {
		// when & then
		InvalidOrderException ex = assertThrows(InvalidOrderException.class, () ->
			commandStockService.restore(9999L, 1)
		);
		assertEquals(BaseResponseStatus.STOCK_RESTORE_FAILED, ex.getStatus());
	}
}