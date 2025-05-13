package com.jelly.zzirit.domain.item.repository;

import java.util.List;
import java.util.Optional;

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

	Optional<Item> findItemWithTypeJoin(Long itemId);
}
