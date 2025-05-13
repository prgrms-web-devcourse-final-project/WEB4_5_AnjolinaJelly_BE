package com.jelly.zzirit.domain.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.global.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryAdminService {
    
    private final ItemRepository itemRepository;

    public PageResponse<AdminItemFetchResponse> getSearchItems(Long itemId, String name, Pageable pageable) {
        Page<AdminItemFetchResponse> page;
        if (itemId != null) {
            page = itemRepository.searchItemById(itemId, pageable);
        } else if (name != null && !name.isBlank()) {
            page = itemRepository.searchItemsByName(name, pageable);
        } else {
            page = itemRepository.findAllItems(pageable);
        }
        return PageResponse.from(page);
    }
}