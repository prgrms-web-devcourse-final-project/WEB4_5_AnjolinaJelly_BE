package com.jelly.zzirit.domain.item.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.item.dto.response.CurrentTimeDealFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.TimeDealCreateResponse;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.PageResponse;
import com.jelly.zzirit.global.exception.custom.InvalidCustomException;

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

		// 겹치는 타임딜이 있는지 검증
		if (isOverlappingTimeDeal(request.startTime(), request.endTime())) {
			throw new InvalidCustomException(BaseResponseStatus.TIME_DEAL_TIME_OVERLAP);
		}

		// 1. 요청 정보로 타임딜을 먼저 저장합니다.
		TimeDeal timeDeal = timeDealRepository.save(
			new TimeDeal(
				request.title(),
				determineTimeDealStatus(request.startTime(), request.endTime(), LocalDateTime.now()),
				request.startTime(),
				request.endTime(),
				request.discountRatio()));

		// 2. 요청으로 들어온 items(id, quantity)와 위에서 저장한 타임딜 정보로 타임딜 아이템을 저장합니다.
		request.items().forEach(item -> {

			// 2-1. 타임딜에 등록된 아이템은 기존 아이템(originItem)내용에 Type만 TIME_DEAL인 새로운 아이템으로 새롭게 저장됩니다.
			Item originItem = itemRepository.findById(item.itemId()).orElseThrow();
			Item clonedItemForTimeDeal = itemRepository.saveAndFlush(new Item(
					originItem.getName(),
					originItem.getImageUrl(),
					originItem.getPrice(),
					ItemStatus.TIME_DEAL,    // 타입만 변경
					originItem.getTypeBrand()
				)
			);

			// 2-2. 저장된 타임딜과, 타입이 타임딜인 아이템을 이용해 중간 엔티티인 타임딜 아이템을 저장합니다.

			// 타임딜 할인율 적용 가격 계산
			BigDecimal discountedPrice = clonedItemForTimeDeal.getPrice().multiply(
				BigDecimal.ONE.subtract(BigDecimal.valueOf(timeDeal.getDiscountRatio()).divide(BigDecimal.valueOf(100)))
			);

			// 타임딜 아이템 저장
			timeDealItemRepository.save(new TimeDealItem(discountedPrice, timeDeal, clonedItemForTimeDeal));

			// 2-3. 요청에 포함된 quantity을 이용해 상품 재고를 저장합니다.
			itemStockRepository.save(new ItemStock(clonedItemForTimeDeal, item.quantity(), item.quantity()));
		});

		// 응답
		List<TimeDealCreateResponse.TimeDealCreateItem> responseItems =
			timeDealItemRepository.findAllByTimeDeal(timeDeal).stream()
				.map(tdi -> {
					Long itemId = tdi.getItem().getId();
					int quantity = itemStockRepository.findByItemId(itemId)
						.map(ItemStock::getQuantity)
						.orElse(0);
					return TimeDealCreateResponse.TimeDealCreateItem.from(itemId, quantity);
				}).toList();

		return TimeDealCreateResponse.from(
			timeDeal.getId(),
			timeDeal.getName(),
			timeDeal.getStartTime().toString(),
			timeDeal.getEndTime().toString(),
			timeDeal.getDiscountRatio(),
			responseItems
		);
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

	private List<CurrentTimeDealFetchResponse.CurrentTimeDealItem> mapToCurrentTimeDealItemList(TimeDeal timeDeal) {
		return timeDealItemRepository.findAllByTimeDeal(timeDeal).stream()
			.map(tdi -> {
				Item item = itemRepository.findById(tdi.getItem().getId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이템입니다."));
				return CurrentTimeDealFetchResponse.CurrentTimeDealItem.from(item, tdi.getPrice());
			})
			.toList();
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

	private TimeDeal.TimeDealStatus determineTimeDealStatus(LocalDateTime start, LocalDateTime end, LocalDateTime now) {
		if (now.isBefore(start)) {
			return TimeDeal.TimeDealStatus.SCHEDULED;
		} else if (!now.isAfter(end)) {
			return TimeDeal.TimeDealStatus.ONGOING;
		} else {
			return TimeDeal.TimeDealStatus.ENDED;
		}
	}

	private boolean isOverlappingTimeDeal(LocalDateTime start, LocalDateTime end) {
		List<TimeDeal> existingDeals = timeDealRepository.findAll();
		return existingDeals.stream().anyMatch(deal ->
			!(deal.getEndTime().isBefore(start) || deal.getStartTime().isAfter(end))
		);
	}
}
