package com.jelly.zzirit.domain.item.service;

import java.util.Comparator;
import java.util.List;

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

	public List<SimpleItemResponse> search(List<String> types, List<String> brands, String keyword, String sort) {
		List<Item> items = (keyword == null || keyword.isBlank())
			? itemRepository.findAll()
			: itemRepository.findAllByNameContainingIgnoreCase(keyword);

		// Step 2. 타입/브랜드로 필터링
		List<Item> filtered = items.stream()
			.filter(item -> types == null || types.isEmpty() || types.contains(item.getTypeBrand().getType().getName()))
			.filter(item -> brands == null || brands.isEmpty() || brands.contains(item.getTypeBrand().getBrand().getName()))
			.toList();

		filtered = sortItems(filtered, sort);

		return filtered.stream()
			.map(this::toSimpleItemResponse)
			.toList();
	}

	private SimpleItemResponse toSimpleItemResponse(Item item) {
		if(item.getItemStatus().equals(ItemStatus.TIME_DEAL)) {
			TimeDealItem timeDealItem = timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId())
				.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));

			return SimpleItemResponse.from(timeDealItem);
		}

		return SimpleItemResponse.from(item);
	}

	private List<Item> sortItems(List<Item> items, String sort) {
		return switch (sort) {
			case "priceDesc" -> items.stream()
				.sorted(Comparator.comparing(Item::getPrice).reversed())
				.toList();
			default -> items.stream()
				.sorted(Comparator.comparing(Item::getPrice))
				.toList();
		};
	}

}
