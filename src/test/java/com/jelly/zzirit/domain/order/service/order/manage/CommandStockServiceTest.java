package com.jelly.zzirit.domain.order.service.order.manage;

import static org.junit.jupiter.api.Assertions.*;

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
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.BrandRepository;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
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

	@MockitoBean
	private AsyncStockHistoryUploader asyncStockHistoryUploader;

	@PersistenceContext
	private EntityManager em;

	private ItemStock savedStock;
	private String orderNumber;

	@BeforeEach
	void setUp() {
		Type type = typeRepository.save(new Type("노트북"));
		Brand brand = brandRepository.save(new Brand("삼성"));
		TypeBrand typeBrand = typeBrandRepository.save(new TypeBrand(type, brand));

		Item item = itemRepository.save(ItemFixture.삼성_노트북(typeBrand));
		ItemStock stock = ItemStockFixture.풀재고_상품(item);
		savedStock = itemStockRepository.save(stock);

		orderNumber = "ORD20240521-000001";
	}

	@Test
	@Transactional
	void 재고가_충분할_때_decrease_정상작동() {
		Long itemId = savedStock.getItem().getId();

		ItemStock before = itemStockRepository.findByItemId(itemId).orElseThrow();

		// when
		commandStockService.decrease(orderNumber, itemId, 3);
		em.flush();
		em.clear();

		// then
		ItemStock after = itemStockRepository.findByItemId(itemId).orElseThrow();
		assertEquals(3, after.getSoldQuantity());
		assertEquals(17, after.getQuantity());
	}

	@Test
	@Transactional
	void 재고가_부족할_때_decrease_실패() {
		Long itemId = savedStock.getItem().getId();

		InvalidOrderException ex = assertThrows(InvalidOrderException.class, () ->
			commandStockService.decrease(orderNumber, itemId, 999)
		);
		assertEquals(BaseResponseStatus.STOCK_REDUCE_FAILED, ex.getStatus());
	}

	@Test
	@Transactional
	void restore_정상작동() {
		Long itemId = savedStock.getItem().getId();

		// given
		commandStockService.decrease(orderNumber, itemId, 5);
		em.flush();
		em.clear();

		ItemStock afterDecrease = itemStockRepository.findByItemId(itemId).orElseThrow();

		// when
		commandStockService.restore(orderNumber, itemId, 2);
		em.flush();
		em.clear();

		// then
		ItemStock finalStock = itemStockRepository.findByItemId(itemId).orElseThrow();
		assertEquals(3, finalStock.getSoldQuantity()); // 5 - 2
		assertEquals(17, finalStock.getQuantity());    // 20 - 5 + 2
	}

	@Test
	@Transactional
	void 존재하지_않는_재고_restore_실패() {
		InvalidOrderException ex = assertThrows(InvalidOrderException.class, () ->
			commandStockService.restore(orderNumber, 9999L, 1)
		);
		assertEquals(BaseResponseStatus.STOCK_RESTORE_FAILED, ex.getStatus());
	}
}