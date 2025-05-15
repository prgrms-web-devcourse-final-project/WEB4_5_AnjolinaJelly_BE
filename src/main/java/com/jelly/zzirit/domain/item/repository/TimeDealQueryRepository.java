package com.jelly.zzirit.domain.item.repository;

import java.util.List;

import com.jelly.zzirit.domain.item.dto.request.TimeDealSearchCondition;
import com.jelly.zzirit.domain.item.dto.response.TimeDealFetchResponse;
import com.jelly.zzirit.global.dto.PageResponse;

public interface TimeDealQueryRepository {
	List<TimeDealFetchResponse> search(TimeDealSearchCondition condition);

	PageResponse<TimeDealFetchResponse> searchWithPaging(TimeDealSearchCondition condition, int page, int size);
}