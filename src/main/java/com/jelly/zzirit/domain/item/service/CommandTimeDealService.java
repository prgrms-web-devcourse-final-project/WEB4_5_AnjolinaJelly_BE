package com.jelly.zzirit.domain.item.service;

import static com.jelly.zzirit.domain.item.util.TimeDealUtil.*;
import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.item.dto.response.CurrentTimeDealFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.CurrentTimeDealFetchResponse.CurrentTimeDealItem;
import com.jelly.zzirit.domain.item.dto.response.TimeDealCreateResponse;
import com.jelly.zzirit.domain.item.dto.response.TimeDealCreateResponse.TimeDealCreateItem;
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

	/**
	 * 타임딜을 생성합니다.
	 * 유효성 검사 → 타임딜 저장 → 타임딜 아이템 및 재고 저장 → 응답 반환
	 *
	 * @param request 타임딜 생성 요청
	 * @return TimeDealCreateResponse 응답 DTO
	 */
	@Transactional
	public TimeDealCreateResponse createTimeDeal(TimeDealCreateRequest request) {
		validateTimeDealRequest(request);
		TimeDeal timeDeal = timeDealRepository.save(TimeDeal.from(request));
		request.items().forEach(item -> createTimeDealItemAndStock(timeDeal, item));
		List<TimeDealCreateItem> responseItems = mapToResponseItems(timeDeal);

		return TimeDealCreateResponse.from(timeDeal, responseItems);
	}

	/**
	 * 현재 진행 중인 타임딜들을 페이징하여 조회합니다.
	 *
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @return PageResponse<CurrentTimeDealFetchResponse> 응답 DTO
	 */
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

	// 타임딜 유효성 검사
	private void validateTimeDealRequest(TimeDealCreateRequest request) {
		LocalDateTime now = LocalDateTime.now();

		if (isStartTimeInPast(request.startTime(), now)) {
			throw new InvalidTimeDealException(TIME_DEAL_START_TIME_PAST);
		}

		if (isOverlappingTimeDeal(request.startTime(), request.endTime())) {
			throw new InvalidTimeDealException(TIME_DEAL_TIME_OVERLAP);
		}
	}

	// 타임딜 아이템 생성 및 해당 재고 저장
	private void createTimeDealItemAndStock(TimeDeal timeDeal, TimeDealCreateRequest.TimeDealCreateItemDetail item) {
		Item originalItem = itemRepository.getById(item.itemId());
		Item clonedItemForTimeDeal = itemRepository.save(Item.from(originalItem));

		BigDecimal discountedPrice = calculateDiscountedPrice(clonedItemForTimeDeal.getPrice(),
			timeDeal.getDiscountRatio());

		timeDealItemRepository.save(new TimeDealItem(discountedPrice, timeDeal, clonedItemForTimeDeal));
		itemStockRepository.save(new ItemStock(clonedItemForTimeDeal, item.quantity(), 0));
	}

	// 응답용 타임딜 아이템 리스트 생성
	private List<TimeDealCreateItem> mapToResponseItems(TimeDeal timeDeal) {
		return timeDealItemRepository.findAllByTimeDeal(timeDeal).stream()
			.map(tdi -> {
				Long itemId = tdi.getItem().getId();
				int quantity = itemStockRepository.findByItemId(itemId)
					.map(ItemStock::getQuantity)
					.orElse(0);
				return TimeDealCreateItem.from(tdi, quantity);
			})
			.toList();
	}

	// 진행중인 타임딜에 해당하는 아이템 응답 리스트 생성
	private List<CurrentTimeDealItem> mapToCurrentTimeDealItemList(TimeDeal timeDeal) {
		return timeDealItemRepository.findAllByTimeDeal(timeDeal).stream()
			.map(this::toCurrentTimeDealItem)
			.toList();
	}

	// 타임딜아이템에서 응답용 아이템 정보로 변환
	private CurrentTimeDealItem toCurrentTimeDealItem(TimeDealItem timeDealItem) {
		Item item = itemRepository.getById(timeDealItem.getItem().getId());
		return CurrentTimeDealItem.from(item, timeDealItem.getPrice());
	}

	// 기존 타임딜과 겹치는 기간이 있는지 여부 확인
	private boolean isOverlappingTimeDeal(LocalDateTime start, LocalDateTime end) {
		List<TimeDeal> existingDeals = timeDealRepository.findAll();
		return existingDeals.stream().anyMatch(deal ->
			isTimeRangeOverlapping(deal.getStartTime(), deal.getEndTime(), start, end)
		);
	}
}
