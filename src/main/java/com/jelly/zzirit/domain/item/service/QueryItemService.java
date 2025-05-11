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
import com.jelly.zzirit.domain.item.repository.ItemQueryRepository;
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
	private final ItemQueryRepository itemQueryRepository;
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
		Page<Item> items = itemQueryRepository.findItems(types, brands, keyword, sort, pageable);

		return new PageResponse<>(
			items.getContent().stream()
				.map(SimpleItemResponse::from)
				.toList(),
			pageable.getPageNumber(),
			pageable.getPageSize(),
			items.getSize(),
			items.getTotalPages(),
			items.isLast()
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
