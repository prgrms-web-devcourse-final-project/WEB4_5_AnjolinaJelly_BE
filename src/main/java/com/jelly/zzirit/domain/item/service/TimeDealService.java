package com.jelly.zzirit.domain.item.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.timeDeal.TimeDealModalItem;
import com.jelly.zzirit.domain.item.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimeDealService {
	private final ItemRepository itemRepository;

	public List<TimeDealModalItem> getModalItems(List<Long> itemIds) {
		return itemRepository.findAllById(itemIds).stream()
			.map(item -> new TimeDealModalItem(item.getId(), item.getName(), Integer.parseInt(
				String.valueOf(item.getPrice()))))
			.toList();
	}
}