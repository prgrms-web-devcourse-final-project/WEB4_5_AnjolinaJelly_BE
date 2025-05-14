package com.jelly.zzirit.domain.item.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jelly.zzirit.domain.item.dto.request.ItemFilterRequest;
import com.jelly.zzirit.domain.item.entity.Item;

public interface ItemQueryRepository {

	Page<Item> findItems(
		ItemFilterRequest filter,
		String sort,
		Pageable pageable
	);
}
