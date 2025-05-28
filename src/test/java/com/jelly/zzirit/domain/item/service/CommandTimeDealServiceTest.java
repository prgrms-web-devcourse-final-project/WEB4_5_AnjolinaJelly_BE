package com.jelly.zzirit.domain.item.service;

import static com.jelly.zzirit.domain.item.domain.fixture.ItemFixture.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import com.jelly.zzirit.domain.item.scheduler.DelayQueueProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.global.exception.custom.InvalidTimeDealException;

public class CommandTimeDealServiceTest {
	@Mock
	private ItemRepository itemRepository;
	@Mock
	private TimeDealRepository timeDealRepository;
	@Mock
	private TimeDealItemRepository timeDealItemRepository;
	@Mock
	private ItemStockRepository itemStockRepository;
	@InjectMocks
	private CommandTimeDealService commandTimeDealService;
	@Mock
	private DelayQueueProcessor delayQueueProcessor;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void 시작시간이_과거인_경우_예외를_던진다() {
		// given
		LocalDateTime now = LocalDateTime.now();
		TimeDealCreateRequest request = new TimeDealCreateRequest(
			"타임딜",
			now.minusMinutes(1),
			now.plusHours(1),
			10,
			List.of()
		);

		// when & then
		assertThatThrownBy(() -> commandTimeDealService.createTimeDeal(request))
			.isInstanceOf(InvalidTimeDealException.class);
	}

	@Test
	void 기존_타임딜과_시간이_겹치면_겹치는_시간이_메시지에_포함된_예외를_던진다() {
		// given
		LocalDateTime baseTime = LocalDateTime.of(2099, 1, 1, 10, 0);

		TimeDealCreateRequest existingRequest = new TimeDealCreateRequest(
			"기존 타임딜",
			baseTime,                          // 10:00
			baseTime.plusHours(1),             // 11:00
			10,
			List.of(TimeDealCreateRequest.TimeDealCreateItemDetail.from(1L, 5))
		);
		TimeDeal existing = TimeDeal.from(existingRequest);
		given(timeDealRepository.findAll()).willReturn(List.of(existing));

		TimeDealCreateRequest request = new TimeDealCreateRequest(
			"새 타임딜",
			baseTime.plusMinutes(30),          // 10:30
			baseTime.plusHours(2),             // 12:00
			10,
			List.of(TimeDealCreateRequest.TimeDealCreateItemDetail.from(2L, 3))
		);

		// when & then
		assertThatThrownBy(() -> commandTimeDealService.createTimeDeal(request))
			.isInstanceOf(InvalidTimeDealException.class)
			.hasMessageContaining("타임 딜 시간을 조정해주세요.")
			.hasMessageContaining("2099.01.01 10:00")
			.hasMessageContaining("11:00");
	}

	@Test
	void 시작시간과_종료시간이_정상이고_겹치지_않으면_예외가_발생하지_않는다() {
		// given
		LocalDateTime now = LocalDateTime.now();
		given(timeDealRepository.findAll()).willReturn(List.of());

		Item originalItem = 삼성_노트북(ItemStatus.NONE);
		Item clonedItem = 삼성_노트북(ItemStatus.TIME_DEAL);

		// Mock: itemRepository getById returns original item
		given(itemRepository.getById(10L)).willReturn(originalItem);
		// Mock: itemRepository save returns cloned item
		given(itemRepository.save(any(Item.class))).willReturn(clonedItem);
		// Mock: timeDealRepository save returns the saved time deal
		given(timeDealRepository.save(any(TimeDeal.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		TimeDealCreateRequest request = new TimeDealCreateRequest(
			"정상 타임딜",
			now.plusMinutes(10),
			now.plusHours(1),
			10,
			List.of(TimeDealCreateRequest.TimeDealCreateItemDetail.from(10L, 5))
		);

		// when & then
		assertThatCode(() -> commandTimeDealService.createTimeDeal(request))
			.doesNotThrowAnyException();
	}
}
