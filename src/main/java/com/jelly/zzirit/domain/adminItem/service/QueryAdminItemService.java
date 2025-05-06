package com.jelly.zzirit.domain.adminItem.service;

import com.jelly.zzirit.domain.adminItem.dto.response.AdminItemResponse;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryAdminItemService {
    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;

    public List<AdminItemResponse> getItems(String name, Long itemId) {

        List<Item> items;

        // 상품id로 검색
        if (itemId != null) {
            // 검색 결과 있는 경우
            Item item = itemRepository.findById(itemId).orElse(null);

            // 검색 결과 없는 경우
            items = item != null ? List.of(item) : List.of();

        // 상품 이름으로 검색
        } else if (name != null && !name.isBlank()) { // blank까지 검사해서 첫검색은 필터링 안되는 문제 해결
            items = itemRepository.findAllByNameContainingIgnoreCase(name);

        // 전체 상품 목록 조회
        } else {
            items = itemRepository.findAll();
        }

        // N+1문제 막기 위해 미리 찾아서 map 만들어둠
        // Todo: item과 item stock의 id를 같게 관리하면 어떨까?
        // Todo: 로직 더 간단히 가능?
        List<Long> itemIds = items.stream().map(Item::getId).toList(); // 상품 id 목록
        List<ItemStock> itemStocks = itemStockRepository.findAllByItemIdIn(itemIds); // 재고 객체 목록
        Map<Item, ItemStock> itemStockMap = itemStocks.stream()
                .collect(Collectors.toMap(ItemStock::getItem, Function.identity()));
        // Map<상품, 재고 객체>

        return items.stream()
                        // from(Item, ItemStock)
                        .map(item -> AdminItemResponse.from(item, itemStockMap.get(item)))
                        .toList();
    }
}