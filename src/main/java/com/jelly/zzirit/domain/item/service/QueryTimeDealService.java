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

	// 타임딜 검색 조건에 따라 타임딜 목록을 조회합니다.
	public List<TimeDealFetchResponse> search(TimeDealSearchCondition condition) {
		List<TimeDealFetchResponse> result = new ArrayList<>();

		if (condition.timeDealName() != null && !condition.timeDealName().isEmpty()) {
			searchByTimeDealName(condition, result);
		}

		if (condition.timeDealId() != null) {
			searchByTimeDealId(condition, result);
		}

		if (condition.timeDealItemName() != null && !condition.timeDealItemName().isEmpty()) {
			searchByTimeDealItemName(condition, result);
		}

		if (condition.timeDealItemId() != null) {
			searchByTimeDealItemId(condition, result);
		}

		if (isAllConditionEmpty(condition)) {
			searchAll(result, condition.status());
		}

		return result;
	}

	private void searchByTimeDealName(TimeDealSearchCondition condition, List<TimeDealFetchResponse> result) {
		List<TimeDeal> timeDeals = timeDealRepository.findByNameContaining(condition.timeDealName());
		timeDeals.forEach(deal -> addFiltered(deal, result, condition.status()));
	}

	private void searchByTimeDealId(TimeDealSearchCondition condition, List<TimeDealFetchResponse> result) {
		TimeDeal deal = timeDealRepository.findById(condition.timeDealId()).orElse(null);
		if (deal != null) {
			addFiltered(deal, result, condition.status());
		}
	}

	private void searchByTimeDealItemName(TimeDealSearchCondition condition, List<TimeDealFetchResponse> result) {
		List<TimeDealItem> items = timeDealItemRepository.findByItem_NameContaining(condition.timeDealItemName());
		items.forEach(item -> {
			TimeDeal deal = item.getTimeDeal();
			if (condition.status() != null && deal.getStatus() != condition.status())
				return;
			int quantity = itemStockRepository.findByItemId(item.getItem().getId())
				.map(q -> q.getQuantity())
				.orElse(0);

			List<TimeDealFetchResponse.TimeDealFetchItem> responseItems = List.of(
				TimeDealFetchResponse.TimeDealFetchItem.from(
					item.getId(),
					item.getItem().getName(),
					quantity,
					item.getItem().getPrice(),
					item.getPrice()
				)
			);
			result.add(TimeDealFetchResponse.from(deal, responseItems));
		});
	}

	private void searchByTimeDealItemId(TimeDealSearchCondition condition, List<TimeDealFetchResponse> result) {
		TimeDealItem item = timeDealItemRepository.findTimeDealItemById(condition.timeDealItemId());
		if (item != null) {
			addFiltered(item.getTimeDeal(), result, condition.status());
		}
	}

	private void searchAll(List<TimeDealFetchResponse> result, TimeDeal.TimeDealStatus status) {
		List<TimeDeal> timeDeals = timeDealRepository.findAll();
		timeDeals.forEach(deal -> addFiltered(deal, result, status));
	}

	private boolean isAllConditionEmpty(TimeDealSearchCondition condition) {
		return condition.timeDealName() == null && condition.timeDealId() == null
			&& condition.timeDealItemName() == null && condition.timeDealItemId() == null;
	}

	// 타임딜 상태 필터링 후 응답 객체를 생성하여 결과 리스트에 추가합니다.
	private void addFiltered(TimeDeal deal, List<TimeDealFetchResponse> result, TimeDeal.TimeDealStatus status) {
		if (status != null && deal.getStatus() != status)
			return;

		List<TimeDealItem> tdItems = timeDealItemRepository.findAllByTimeDeal(deal);
		List<TimeDealFetchResponse.TimeDealFetchItem> items = tdItems.stream()
			.map(item -> {
				int quantity = itemStockRepository.findByItemId(item.getItem().getId())
					.map(q -> q.getQuantity())
					.orElse(0);
				return TimeDealFetchResponse.TimeDealFetchItem.from(
					item.getId(),
					item.getItem().getName(),
					quantity,
					item.getItem().getPrice(),
					item.getPrice()
				);
			})
			.toList();
		result.add(TimeDealFetchResponse.from(deal, items));
	}

	// 타임딜 관리자 검색 요청을 처리하고 페이징 결과를 반환합니다.
	public PageResponse<TimeDealFetchResponse> getTimeDeals(
		String timeDealName,
		Long timeDealId,
		String timeDealItemName,
		Long timeDealItemId,
		TimeDeal.TimeDealStatus status,
		int page,
		int size
	) {
		TimeDealSearchCondition condition = TimeDealSearchCondition.from(
			timeDealName, timeDealId, timeDealItemName, timeDealItemId, status);
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