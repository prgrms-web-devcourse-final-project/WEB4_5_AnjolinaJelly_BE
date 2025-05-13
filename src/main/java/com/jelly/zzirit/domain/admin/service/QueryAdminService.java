package com.jelly.zzirit.domain.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemQueryRepository;
import com.jelly.zzirit.global.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryAdminService {
    
    private final ItemRepository itemRepository;
    private final ItemQueryRepository itemQueryRepository;

    public PageResponse<AdminItemFetchResponse> getSearchItems(Long itemId, String name, Pageable pageable) {
        Page<AdminItemFetchResponse> page;
        if (itemId != null) {
            page = itemQueryRepository.findAdminItems(null, itemId, pageable);
        } else if (name != null && !name.isBlank()) {
            page = itemQueryRepository.findAdminItems(name, null, pageable);
        } else {
            page = itemQueryRepository.findAdminItems(null, null, pageable);
        }
        return PageResponse.from(page);
    }
}