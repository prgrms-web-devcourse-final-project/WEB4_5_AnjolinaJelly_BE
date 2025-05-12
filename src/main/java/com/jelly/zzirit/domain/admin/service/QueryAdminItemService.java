package com.jelly.zzirit.domain.admin.service;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemResponse;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;

import com.jelly.zzirit.global.dto.BaseResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.global.dto.PageResponse;

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

    public PageResponse<AdminItemResponse> getSearchItems(Long itemId, String name, Pageable pageable) {
        Page<AdminItemResponse> page;
        if (itemId != null) {
            page = itemRepository.searchItemById(itemId, pageable);
        } else if (name != null && !name.isBlank()) {
            page = itemRepository.searchItemsByName(name, pageable);
        } else {
            page = itemRepository.findAllItems(pageable);
        }
        return PageResponse.of(page);
    }
}