package com.jelly.zzirit.domain.item.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.dto.request.TimeDealSearchCondition;
import com.jelly.zzirit.domain.item.dto.response.TimeDealFetchResponse;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.TimeDealQueryRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.global.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueryTimeDealService {

	private final TimeDealRepository timeDealRepository;
	private final TimeDealQueryRepository timeDealQueryRepository;

	// 타임딜 검색 조건에 따라 타임딜 목록을 조회합니다.
	public List<TimeDealFetchResponse> search(TimeDealSearchCondition condition) {
		return timeDealQueryRepository.search(condition);
	}

	// 타임딜 관리자 검색 요청을 처리하고 페이징 결과를 반환합니다.
	public PageResponse<TimeDealFetchResponse> getTimeDeals(
		String timeDealName,
		Long timeDealId,
		String timeDealItemName,
		Long timeDealItemId,
		TimeDeal.TimeDealStatus status,
		int page,
		int size
	) {
		TimeDealSearchCondition condition = TimeDealSearchCondition.from(
			timeDealName, timeDealId, timeDealItemName, timeDealItemId, status);

		return timeDealQueryRepository.searchWithPaging(condition, page, size);
	}

}