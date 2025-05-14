package com.jelly.zzirit.domain.item.service;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.item.dto.response.CurrentTimeDealFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.TimeDealCreateResponse;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.global.dto.PageResponse;
import com.jelly.zzirit.global.exception.custom.InvalidTimeDealException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommandTimeDealService {
	private final ItemRepository itemRepository;
	private final TimeDealRepository timeDealRepository;
	private final TimeDealItemRepository timeDealItemRepository;
	private final ItemStockRepository itemStockRepository;

	// 타임딜 생성
	@Transactional
	public TimeDealCreateResponse createTimeDeal(TimeDealCreateRequest request) {

		// 겹치는 타임딜이 있는지 & 시작 시간이 과거인지 검증
		validateTimeDealRequest(request);

		// 1. 요청 정보로 타임딜을 먼저 저장합니다.
		TimeDeal timeDeal = timeDealRepository.save(TimeDeal.from(request));

		// 2. 요청으로 들어온 items(id, quantity)와 위에서 저장한 타임딜 정보로 타임딜 아이템을 저장합니다.
		request.items().forEach(item -> createTimeDealItemAndStock(timeDeal, item));

		// 3. 응답 생성
		List<TimeDealCreateResponse.TimeDealCreateItem> responseItems = mapToResponseItems(timeDeal);

		return TimeDealCreateResponse.from(timeDeal, responseItems);
	}

	// 진행중인 타임딜 조회
	public PageResponse<CurrentTimeDealFetchResponse> getCurrentTimeDeals(int page, int size) {
		List<CurrentTimeDealFetchResponse> fullList = timeDealRepository.getOngoingTimeDeal().stream()
			.map(timeDeal -> CurrentTimeDealFetchResponse.from(
				timeDeal,
				mapToCurrentTimeDealItemList(timeDeal)
			))
			.toList();

		int start = page * size;
		int end = Math.min(start + size, fullList.size());
		List<CurrentTimeDealFetchResponse> pagedResult = fullList.subList(start, end);

		return new PageResponse<>(
			pagedResult,
			page,
			size,
			fullList.size(),
			(int)Math.ceil((double)fullList.size() / size),
			end >= fullList.size()
		);
	}

	@Transactional
	public int convertTimeDealStatusScheduledToOngoing(LocalDateTime now) {
		// 시작 시간이 지났지만 아직 시작되지 않은 타임딜 (SCHEDULED → ONGOING)
		List<TimeDeal> toStartDeals = timeDealRepository.findAllByStatusAndStartTimeLessThanEqual(
			TimeDeal.TimeDealStatus.SCHEDULED, now);
		toStartDeals.forEach(deal -> deal.updateStatus(TimeDeal.TimeDealStatus.ONGOING));

		return toStartDeals.size();
	}

	@Transactional
	public int convertTimeDealStatusOngoingToEnded(LocalDateTime now) {
		// 종료 시간이 지난 타임딜 (ONGOING → ENDED)
		List<TimeDeal> toEndDeals = timeDealRepository.findAllByStatusAndEndTimeBefore(TimeDeal.TimeDealStatus.ONGOING,
			now);
		toEndDeals.forEach(deal -> deal.updateStatus(TimeDeal.TimeDealStatus.ENDED));

		return toEndDeals.size();
	}

	// 타임딜 유효성 검사
	private void validateTimeDealRequest(TimeDealCreateRequest request) {
		LocalDateTime now = LocalDateTime.now();

		if (request.startTime().isBefore(now)) {
			throw new InvalidTimeDealException(TIME_DEAL_START_TIME_PAST);
		}

		if (isOverlappingTimeDeal(request.startTime(), request.endTime())) {
			throw new InvalidTimeDealException(TIME_DEAL_TIME_OVERLAP);
		}
	}

	private void createTimeDealItemAndStock(TimeDeal timeDeal, TimeDealCreateRequest.TimeDealCreateItemDetail item) {
		Item originalItem = itemRepository.getById(item.itemId());
		Item clonedItemForTimeDeal = itemRepository.save(Item.from(originalItem));

		BigDecimal discountedPrice = calculateDiscountedPrice(clonedItemForTimeDeal.getPrice(),
			timeDeal.getDiscountRatio());

		timeDealItemRepository.save(new TimeDealItem(discountedPrice, timeDeal, clonedItemForTimeDeal));

		itemStockRepository.save(new ItemStock(clonedItemForTimeDeal, item.quantity(), 0));
	}

	// 응답 생성
	private List<TimeDealCreateResponse.TimeDealCreateItem> mapToResponseItems(TimeDeal timeDeal) {
		return timeDealItemRepository.findAllByTimeDeal(timeDeal).stream()
			.map(tdi -> {
				Long itemId = tdi.getItem().getId();
				int quantity = itemStockRepository.findByItemId(itemId)
					.map(ItemStock::getQuantity)
					.orElse(0);
				return TimeDealCreateResponse.TimeDealCreateItem.from(itemId, quantity);
			}).toList();
	}

	private List<CurrentTimeDealFetchResponse.CurrentTimeDealItem> mapToCurrentTimeDealItemList(TimeDeal timeDeal) {
		return timeDealItemRepository.findAllByTimeDeal(timeDeal).stream()
			.map(tdi -> {
				Item item = itemRepository.findById(tdi.getItem().getId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이템입니다."));
				return CurrentTimeDealFetchResponse.CurrentTimeDealItem.from(item, tdi.getPrice());
			})
			.toList();
	}

	private boolean isOverlappingTimeDeal(LocalDateTime start, LocalDateTime end) {
		List<TimeDeal> existingDeals = timeDealRepository.findAll();
		return existingDeals.stream().anyMatch(deal ->
			!(deal.getEndTime().isBefore(start) || deal.getStartTime().isAfter(end))
		);
	}

	private BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, int discountRatio) {
		BigDecimal discountRate = BigDecimal.valueOf(discountRatio).divide(BigDecimal.valueOf(100));
		return originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
	}
}
