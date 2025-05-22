package com.jelly.zzirit.domain.order.service.order.manage;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.domain.fixture.ItemFixture;
import com.jelly.zzirit.domain.item.domain.fixture.ItemStockFixture;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.BrandRepository;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;
import com.jelly.zzirit.domain.item.repository.TypeRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.order.service.order.CommandStockService;
import com.jelly.zzirit.domain.order.util.AsyncStockHistoryUploader;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import com.jelly.zzirit.global.redis.TestContainerConfig;
import com.jelly.zzirit.global.redis.TestRedisTemplateConfig;
import com.jelly.zzirit.global.redis.TestRedissonConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@SpringBootTest
@ActiveProfiles("test")
@Import({TestRedisTemplateConfig.class, TestRedissonConfig.class})
class CommandStockServiceTest extends TestContainerConfig {

	@Autowired
	private CommandStockService commandStockService;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private ItemStockRepository itemStockRepository;

	@Autowired
	private TypeRepository typeRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private TypeBrandRepository typeBrandRepository;

	@Autowired
	private TimeDealItemRepository timeDealItemRepository;

	@Autowired
	private TimeDealRepository timeDealRepository;

	@MockitoBean
	private AsyncStockHistoryUploader asyncStockHistoryUploader;

	@PersistenceContext
	private EntityManager em;

	private Item savedItem;
	private ItemStock savedStock;
	private String orderNumber;

	@BeforeEach
	void setUp() {
		Type type = typeRepository.save(new Type("노트북"));
		Brand brand = brandRepository.save(new Brand("삼성"));
		TypeBrand typeBrand = typeBrandRepository.save(new TypeBrand(type, brand));

		savedItem = itemRepository.save(ItemFixture.삼성_노트북(typeBrand));
		ItemStock stock = ItemStockFixture.풀재고_상품(savedItem);
		savedStock = itemStockRepository.save(stock);

		orderNumber = "ORD20240521-000001";
	}

	@Test
	@Transactional
	void 재고가_충분할_때_decrease_정상작동() {
		Long itemId = savedItem.getId();

		ItemStock before = itemStockRepository.findByItemId(itemId).orElseThrow();

		commandStockService.decrease(orderNumber, itemId, 3);
		em.flush(); em.clear();

		ItemStock after = itemStockRepository.findByItemId(itemId).orElseThrow();
		assertEquals(3, after.getSoldQuantity());
		assertEquals(17, after.getQuantity());
	}

	@Test
	@Transactional
	void 재고가_부족할_때_decrease_실패() {
		Long itemId = savedItem.getId();

		InvalidOrderException ex = assertThrows(InvalidOrderException.class, () ->
			commandStockService.decrease(orderNumber, itemId, 999)
		);
		assertEquals(BaseResponseStatus.STOCK_REDUCE_FAILED, ex.getStatus());
	}

	@Test
	@Transactional
	void restore_정상작동() {
		Long itemId = savedItem.getId();

		commandStockService.decrease(orderNumber, itemId, 5);
		em.flush(); em.clear();

		commandStockService.restore(orderNumber, itemId, 2);
		em.flush(); em.clear();

		ItemStock finalStock = itemStockRepository.findByItemId(itemId).orElseThrow();
		assertEquals(3, finalStock.getSoldQuantity());
		assertEquals(17, finalStock.getQuantity());
	}

	@Test
	@Transactional
	void 존재하지_않는_아이템_restore_실패() {
		InvalidOrderException ex = assertThrows(InvalidOrderException.class, () ->
			commandStockService.restore(orderNumber, 9999L, 1)
		);
		assertEquals(BaseResponseStatus.ITEM_NOT_FOUND, ex.getStatus()); // 수정됨
	}

	@Test
	@Transactional
	void 타임딜상품_decrease_restore_정상작동() {
		// given
		savedItem.changeItemStatus(ItemStatus.TIME_DEAL);

		TimeDeal deal = timeDealRepository.save(TimeDeal.builder()
			.name("타임딜")
			.status(TimeDeal.TimeDealStatus.ONGOING)
			.startTime(LocalDateTime.now().minusHours(1))
			.endTime(LocalDateTime.now().plusHours(1))
			.discountRatio(20)
			.build()
		);

		TimeDealItem timeDealItem = timeDealItemRepository.save(TimeDealItem.builder()
			.item(savedItem)
			.timeDeal(deal)
			.price(new BigDecimal("8000"))
			.build()
		);

		ItemStock timeDealStock = itemStockRepository.save(ItemStockFixture.풀재고_타임딜상품(timeDealItem));
		Long itemId = savedItem.getId();

		// when
		commandStockService.decrease(orderNumber, itemId, 4);
		em.flush(); em.clear();

		commandStockService.restore(orderNumber, itemId, 2);
		em.flush(); em.clear();

		// then
		ItemStock after = itemStockRepository.findByTimeDealItemId(timeDealItem.getId()).orElseThrow();
		assertEquals(2, after.getSoldQuantity());
		assertEquals(18, after.getQuantity());
	}
}