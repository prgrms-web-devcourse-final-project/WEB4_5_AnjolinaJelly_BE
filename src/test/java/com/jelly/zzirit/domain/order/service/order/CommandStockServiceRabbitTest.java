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
class CommandStockServiceRabbitTest extends TestContainerConfig {

	@Autowired
	private CommandStockService commandStockService;

	@Autowired
	private ItemStockRepository itemStockRepository;

	@Transactional
	@Test
	void 분산락_적용된_상태에서_정상적으로_판매수량이_증가된다() {
		// given
		Long itemId = 1L;

		// when
		commandStockService.decrease(itemId, 3);

		// then
		ItemStock stock = itemStockRepository.findByItemId(itemId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.STOCK_NOT_FOUND));

		assertEquals(3, stock.getSoldQuantity());
	}
}