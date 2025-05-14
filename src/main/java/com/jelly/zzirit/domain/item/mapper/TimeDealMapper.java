package com.jelly.zzirit.domain.item.mapper;

import com.jelly.zzirit.domain.item.dto.response.TimeDealCreateResponse;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

public class TimeDealMapper {
	// 타임딜아이템에서 응답용 생성 아이템 정보로 변환 (수량은 인자로 주입)
	public static TimeDealCreateResponse.TimeDealCreateItem toTimeDealCreateItem(TimeDealItem timeDealItem,
		int quantity) {
		Long itemId = timeDealItem.getItem().getId();
		return TimeDealCreateResponse.TimeDealCreateItem.from(itemId, quantity);
	}
}
