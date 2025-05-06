package com.jelly.zzirit.domain.item.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.response.ItemResponse;
import com.jelly.zzirit.domain.item.dto.response.SimpleItemResponse;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.service.QueryItemService;
import com.jelly.zzirit.global.dto.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
@Tag(name = "상품 API", description = "상품과 관련된 API를 설명합니다.")
public class ItemController {

	private final QueryItemService queryItemService;

	@GetMapping("/search")
	@Operation(summary = "상품 조회 및 검색", description = "상품을 조회하고 검색합니다.")
	public BaseResponse<List<SimpleItemResponse>> search(
		@RequestParam(required = false) List<String> type,
		@RequestParam(required = false) List<String> brands,
		@RequestParam(required = false) String keyword,
		@RequestParam(defaultValue = "priceAsc") String sort
	) {
		return BaseResponse.success(
			queryItemService.search(type, brands, keyword, sort)
		);
	}

	@GetMapping("/{item-id}")
	@Operation(summary = "상품 상세 조회", description = "상품을 상세 조회 합니다.")
	public BaseResponse<ItemResponse> getById(@PathVariable(name = "item-id") Long itemId) {
		return BaseResponse.success(
			queryItemService.getById(itemId)
		);
	}
}
