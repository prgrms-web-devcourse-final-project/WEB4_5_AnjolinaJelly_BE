package com.jelly.zzirit.domain.item.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.jelly.zzirit.global.dto.PageResponse;

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

		TimeDeal timeDeal = timeDealRepository.save(
			new TimeDeal(
				request.title(),
				TimeDeal.TimeDealStatus.SCHEDULED,
				request.startTime(),
				request.endTime(),
				request.discountRatio()
			)
		);

		List<TimeDealCreateResponse.TimeDealCreateItem> responseItems = new ArrayList<>();

		request.items().forEach(item -> {
			Item originItem = itemRepository.findById(item.itemId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

			Item clonedItem = itemRepository.save(new Item(
				originItem.getName(),
				originItem.getImageUrl(),
				originItem.getPrice(),
				ItemStatus.TIME_DEAL,
				originItem.getTypeBrand()
			));

			BigDecimal discountedPrice = clonedItem.getPrice().multiply(
				BigDecimal.ONE.subtract(BigDecimal.valueOf(timeDeal.getDiscountRatio()).divide(BigDecimal.valueOf(100)))
			);

			timeDealItemRepository.save(new TimeDealItem(discountedPrice, timeDeal, clonedItem));
			itemStockRepository.save(new ItemStock(clonedItem, item.quantity(), item.quantity()));

			responseItems.add(TimeDealCreateResponse.TimeDealCreateItem.from(clonedItem.getId(), item.quantity()));
		});

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
}