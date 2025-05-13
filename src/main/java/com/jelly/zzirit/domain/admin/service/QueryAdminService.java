package com.jelly.zzirit.domain.admin.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.global.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryAdminService {
    
    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;

    public PageResponse<AdminItemFetchResponse> getItems(String name, Long itemId, Pageable pageable) {

        Page<Item> itemsPage;

        // 상품id로 검색
        if (itemId != null) {
            // 검색 결과 있는 경우
            Item item = itemRepository.findById(itemId).orElse(null);

            // 검색 결과 없는 경우
            List<Item> itemList = (item != null) ? List.of(item) : List.of();
            itemsPage = new PageImpl<>(itemList, pageable, itemList.size());

            // 상품 이름으로 검색
        } else if (name != null && !name.isBlank()) { // blank까지 검사해서 첫검색은 필터링 안되는 문제 해결
            itemsPage = itemRepository.findAllByNameContainingIgnoreCase(name, pageable);

        // 전체 상품 목록 조회
        } else {
            itemsPage = itemRepository.findAll(pageable);
        }

        // N+1문제 막기 위해 미리 찾아서 map 만들어둠
        // Todo: item과 item stock의 id를 같게 관리하면 어떨까?
        // Todo: 로직 더 간단히 가능?
        List<Long> itemIds = itemsPage.getContent().stream()
            .map(Item::getId)
            .toList();

        List<ItemStock> itemStocks = itemStockRepository.findAllByItemIdIn(itemIds);
        Map<Item, ItemStock> itemStockMap = itemStocks.stream()
            .collect(Collectors.toMap(ItemStock::getItem, Function.identity()));

        List<AdminItemFetchResponse> content = itemsPage.getContent().stream()
            .map(item -> AdminItemFetchResponse.from(item, itemStockMap.get(item)))
            .toList();

        return new PageResponse<>(
            content,
            itemsPage.getNumber(),
            itemsPage.getSize(),
            itemsPage.getTotalElements(),
            itemsPage.getTotalPages(),
            itemsPage.isLast()
        );
    }
}