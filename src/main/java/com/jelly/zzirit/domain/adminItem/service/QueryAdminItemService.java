package com.jelly.zzirit.domain.adminItem.service;

import com.jelly.zzirit.domain.adminItem.dto.response.AdminItemResponse;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.order.repository.ItemStockRepository;
import com.jelly.zzirit.global.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.jelly.zzirit.domain.item.repository.ItemRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryAdminItemService {
    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;

    public BaseResponse<List<AdminItemResponse>> getItems() {
        List<Item> items = itemRepository.findAll();

        // N+1문제 막기 위해 미리 찾아서 map 만들어둠
        // Todo: item과 item stock의 id를 같게 관리하면 어떨까?
        List<Long> itemIds = items.stream().map(Item::getId).toList(); // 상품 id 목록
        List<ItemStock> itemStocks = itemStockRepository.findAllByItemIdIn(itemIds); // 재고 개수 목록
        Map<Long, ItemStock> itemStockMap = itemStocks.stream().collect(Collectors.toMap(ItemStock::getId, Function.identity()));
        // Map<재고 id, 재고 객체>


        return BaseResponse.success(
                items.stream()
                        // from(Item, ItemStock)
                        .map(item -> AdminItemResponse.from(item, itemStockMap.get(item.getId())))
                        .toList());
    }
}