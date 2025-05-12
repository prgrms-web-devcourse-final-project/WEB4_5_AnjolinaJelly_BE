package com.jelly.zzirit.domain.item.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.response.ItemFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.SimpleItemFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.CurrentTimeDealFetchResponse;
import com.jelly.zzirit.domain.item.service.QueryItemService;
import com.jelly.zzirit.domain.item.service.CommandTimeDealService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Tag(name = "상품 API", description = "상품과 관련된 API를 설명합니다.")
public class ItemController {

	private final QueryItemService queryItemService;
	private final CommandTimeDealService timeDealService;

	@GetMapping("/search")
	@Operation(summary = "상품 조회 및 검색", description = "상품을 조회하고 검색합니다.")
	public BaseResponse<PageResponse<SimpleItemFetchResponse>> search(
		@RequestParam(required = false) List<String> types,
		@RequestParam(required = false) List<String> brands,
		@RequestParam(required = false) String keyword,
		@RequestParam(defaultValue = "priceAsc") String sort,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		return BaseResponse.success(
			PageResponse.from(
				queryItemService.search(types, brands, keyword, sort, pageable)
					.map(SimpleItemFetchResponse::from)
			)
		);
	}

	@GetMapping("/{item-id}")
	@Operation(summary = "상품 상세 조회", description = "상품을 상세 조회 합니다.")
	public BaseResponse<ItemFetchResponse> getById(@PathVariable(name = "item-id") Long itemId) {
		return BaseResponse.success(
			queryItemService.getById(itemId)
		);
	}

	@GetMapping("/time-deals/now")
	@Operation(summary = "현재 진행 중인 타임딜", description = "현재 진행중인 타임딜 및 타임딜 상품을 조회합니다.")
	public BaseResponse<PageResponse<CurrentTimeDealFetchResponse>> getCurrentTimeDeals(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "3") int size
	) {
		PageResponse<CurrentTimeDealFetchResponse> response = timeDealService.getCurrentTimeDeals(page, size);
		return BaseResponse.success(response);
	}
}
