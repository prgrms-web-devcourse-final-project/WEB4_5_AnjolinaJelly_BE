package com.jelly.zzirit.domain.item.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.item.dto.response.timeDeal.CurrentTimeDealResponse;
import com.jelly.zzirit.domain.item.dto.response.timeDeal.TimeDealCreateResponse;
import com.jelly.zzirit.domain.item.dto.response.timeDeal.TimeDealModalCreateResponse;
import com.jelly.zzirit.domain.item.dto.response.timeDeal.TimeDealSearchResponse;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.service.TimeDealService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "타임딜 상품 API", description = "타임딜 기능을 제공합니다.")
public class TimeDealController {
	private final TimeDealService timeDealService;

	@Operation(summary = "타임딜 등록", description = "타임딜 정보와 아이템 리스트를 등록합니다.")
	@PostMapping("/api/admin/time-deals")
	public BaseResponse<TimeDealCreateResponse> createTimeDeal(@RequestBody TimeDealCreateRequest request) {
		TimeDealCreateResponse response = timeDealService.createTimeDeal(request);
		return BaseResponse.success(response);
	}

	@PostMapping("/api/admin/time-deals/modal")
	@Operation(summary = "타임딜 생성 모달 상품 조회", description = "타임딜 저장을 위한 모달을 생성합니다")
	public BaseResponse<List<TimeDealModalCreateResponse>> getTimeDealModalItems(@RequestBody List<Long> itemIds) {
		List<TimeDealModalCreateResponse> result = timeDealService.getModalItems(itemIds);
		return BaseResponse.success(result);
	}

	@GetMapping("/api/time-deals/now")
	@Operation(summary = "현재 진행 중인 타임딜", description = "현재 진행중인 타임딜 및 타임딜 상품을 조회합니다.")
	public BaseResponse<PageResponse<CurrentTimeDealResponse>> getCurrentTimeDeals(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "3") int size
	) {
		PageResponse<CurrentTimeDealResponse> response = timeDealService.getCurrentTimeDeals(page, size);
		return BaseResponse.success(response);
	}

	@GetMapping("/api/admin/time-deals/search")
	@Operation(summary = "(관리자 페이지)타임딜 목록 조회", description = "관리자 페이지에서 타임딜 목록을 조회합니다.")
	public BaseResponse<PageResponse<TimeDealSearchResponse>> searchTimeDeals(
		@RequestParam(required = false) String timeDealName,
		@RequestParam(required = false) Long timeDealId,
		@RequestParam(required = false) String timeDealItemName,
		@RequestParam(required = false) Long timeDealItemId,
		@RequestParam(required = false) TimeDeal.TimeDealStatus status,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		PageResponse<TimeDealSearchResponse> result = timeDealService.getTimeDeals(
			timeDealName, timeDealId, timeDealItemName, timeDealItemId, status, page, size
		);
		return BaseResponse.success(result);
	}
}
