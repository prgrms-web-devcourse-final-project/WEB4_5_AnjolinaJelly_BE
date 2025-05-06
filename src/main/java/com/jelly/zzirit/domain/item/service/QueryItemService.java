package com.jelly.zzirit.domain.item.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.response.ItemResponse;
import com.jelly.zzirit.domain.item.dto.response.SimpleItemResponse;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.PageResponse;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryItemService {

	private final ItemRepository itemRepository;
	private final ItemStockRepository itemStockRepository;
	private final TimeDealItemRepository timeDealItemRepository;

	public ItemResponse getById(Long itemId) {
		Item item = itemRepository.getById(itemId);
		ItemStock itemStock = itemStockRepository.findByItemId(item.getId())
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));

		if(item.getItemStatus().equals(ItemStatus.TIME_DEAL)) {
			TimeDealItem timeDealItem = timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId())
				.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));

			return ItemResponse.from(timeDealItem, itemStock.getQuantity());
		}

		return ItemResponse.from(item, itemStock.getQuantity());
	}

	public PageResponse<SimpleItemResponse> search(List<String> types, List<String> brands, String keyword, String sort, Pageable pageable) {
		Page<Item> rawItems = (keyword == null || keyword.isBlank())
			? itemRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE))  // 전체 데이터 조회
			: itemRepository.findAllByNameContainingIgnoreCase(keyword, PageRequest.of(0, Integer.MAX_VALUE));

		// 타입, 브랜드 필터링
		List<Item> filtered = rawItems.stream()
			.filter(item -> types == null || types.isEmpty() || types.contains(item.getTypeBrand().getType().getName()))
			.filter(item -> brands == null || brands.isEmpty() || brands.contains(item.getTypeBrand().getBrand().getName()))
			.toList();

		// 정렬
		List<Item> sorted = switch (sort) {
			case "priceDesc" -> filtered.stream().sorted(Comparator.comparing(Item::getPrice).reversed()).toList();
			default -> filtered.stream().sorted(Comparator.comparing(Item::getPrice)).toList();
		};

		// 페이징
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), sorted.size());
		List<SimpleItemResponse> pageContent = sorted.subList(start, end).stream()
			.map(this::toSimpleItemResponse)
			.toList();

		return new PageResponse<>(
			pageContent,
			pageable.getPageNumber(),
			pageable.getPageSize(),
			sorted.size(),
			(int) Math.ceil((double) sorted.size() / pageable.getPageSize()),
			end == sorted.size()
		);
	}

	private SimpleItemResponse toSimpleItemResponse(Item item) {
		if(item.getItemStatus().equals(ItemStatus.TIME_DEAL)) {
			TimeDealItem timeDealItem = timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId())
				.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));

			return SimpleItemResponse.from(timeDealItem);
		}

		return SimpleItemResponse.from(item);
	}
}
