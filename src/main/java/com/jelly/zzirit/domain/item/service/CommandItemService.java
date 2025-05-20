package com.jelly.zzirit.domain.item.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CommandItemService {

	private final ItemRepository itemRepository;
	private final TimeDealItemRepository timeDealItemRepository;

	public void updateItemStatusByTimeDeal(TimeDeal timeDeal, ItemStatus status) {
		timeDealItemRepository.findAllByTimeDeal(timeDeal).stream()
			.map(tdi -> tdi.getItem())
			.distinct()
			.forEach(item -> item.changeItemStatus(status));
	}
}
