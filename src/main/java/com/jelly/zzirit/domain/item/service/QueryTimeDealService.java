package com.jelly.zzirit.domain.item.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.dto.request.TimeDealSearchCondition;
import com.jelly.zzirit.domain.item.dto.response.TimeDealFetchResponse;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.global.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueryTimeDealService {

	private final TimeDealRepository timeDealRepository;
	private final TimeDealItemRepository timeDealItemRepository;
	private final ItemStockRepository itemStockRepository;

	public List<TimeDealFetchResponse> search(TimeDealSearchCondition condition) {
		List<TimeDealFetchResponse> result = new ArrayList<>();

		if (condition.timeDealName() != null && !condition.timeDealName().isEmpty()) {
			List<TimeDeal> timeDeals = timeDealRepository.findByNameContaining(condition.timeDealName());
			timeDeals.forEach(deal -> addFiltered(deal, result, condition.status()));
		}

		if (condition.timeDealId() != null) {
			TimeDeal deal = timeDealRepository.findById(condition.timeDealId()).orElse(null);
			if (deal != null) {
				addFiltered(deal, result, condition.status());
			}
		}

		if (condition.timeDealItemName() != null && !condition.timeDealItemName().isEmpty()) {
			List<TimeDealItem> items = timeDealItemRepository.findByItem_NameContaining(condition.timeDealItemName());
			items.forEach(item -> {
				TimeDeal deal = item.getTimeDeal();
				if (condition.status() != null && deal.getStatus() != condition.status())
					return;
				List<TimeDealFetchResponse.TimeDealFetchItem> responseItems = List.of(toSearchItem(item));
				result.add(toSearchDeal(deal, responseItems));
			});
		}

		if (condition.timeDealItemId() != null) {
			TimeDealItem item = timeDealItemRepository.findTimeDealItemById(condition.timeDealItemId());
			if (item != null) {
				addFiltered(item.getTimeDeal(), result, condition.status());
			}
		}

		if (condition.timeDealName() == null && condition.timeDealId() == null &&
			condition.timeDealItemName() == null && condition.timeDealItemId() == null) {
			List<TimeDeal> timeDeals = timeDealRepository.findAll();
			timeDeals.forEach(deal -> addFiltered(deal, result, condition.status()));
		}

		return result;
	}

	private void addFiltered(TimeDeal deal, List<TimeDealFetchResponse> result, TimeDeal.TimeDealStatus status) {
		if (status != null && deal.getStatus() != status)
			return;

		List<TimeDealItem> tdItems = timeDealItemRepository.findAllByTimeDeal(deal);
		List<TimeDealFetchResponse.TimeDealFetchItem> items = tdItems.stream()
			.map(this::toSearchItem)
			.toList();
		result.add(toSearchDeal(deal, items));
	}

	private TimeDealFetchResponse.TimeDealFetchItem toSearchItem(TimeDealItem timeDealItem) {
		int quantity = itemStockRepository.findByItemId(timeDealItem.getItem().getId())
			.map(q -> q.getQuantity())
			.orElse(0);

		return TimeDealFetchResponse.TimeDealFetchItem.from(
			timeDealItem.getId(),
			timeDealItem.getItem().getName(),
			quantity,
			timeDealItem.getItem().getPrice(),
			timeDealItem.getPrice()
		);
	}

	private TimeDealFetchResponse toSearchDeal(TimeDeal deal, List<TimeDealFetchResponse.TimeDealFetchItem> items) {
		return TimeDealFetchResponse.from(deal, items);
	}

	// 타임틸 관리자 조회
	public PageResponse<TimeDealFetchResponse> getTimeDeals(
		String timeDealName,
		Long timeDealId,
		String timeDealItemName,
		Long timeDealItemId,
		TimeDeal.TimeDealStatus status,
		int page,
		int size
	) {
		TimeDealSearchCondition condition = TimeDealSearchCondition.from(timeDealName, timeDealId, timeDealItemName,
			timeDealItemId, status);
		List<TimeDealFetchResponse> result = search(condition);

		// 페이징 처리
		int start = page * size;
		int end = Math.min(start + size, result.size());
		List<TimeDealFetchResponse> pagedResult = result.subList(start, end);

		return new PageResponse<>(
			pagedResult,
			page,
			size,
			result.size(),
			(int)Math.ceil((double)result.size() / size),
			end >= result.size()
		);
	}

}