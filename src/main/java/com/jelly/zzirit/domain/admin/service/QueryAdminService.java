package com.jelly.zzirit.domain.admin.service;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.item.repository.ItemQueryRepository;
import com.jelly.zzirit.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryAdminService {
    
    private final ItemQueryRepository itemQueryRepository;

    public Optional<AdminItemFetchResponse> getItemById(Long itemId) {
        return itemQueryRepository.findAdminItemById(itemId);
    }

    public PageResponse<AdminItemFetchResponse> getSearchItems(String name, String sort, Pageable pageable) {
        return PageResponse.from(
                itemQueryRepository.findAdminItems(name, sort, pageable)
        );
    }
}