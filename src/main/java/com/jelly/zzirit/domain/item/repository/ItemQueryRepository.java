package com.jelly.zzirit.domain.item.repository;

import java.util.List;
import java.util.Optional;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jelly.zzirit.domain.item.entity.Item;

public interface ItemQueryRepository {

	Page<Item> findItems(
		List<String> types,
		List<String> brands,
		String keyword,
		String sort,
		Pageable pageable
	);

	Optional<AdminItemFetchResponse> findAdminItemById(Long itemId);

	Page<AdminItemFetchResponse> findAdminItems(String name, Pageable pageable);
}
