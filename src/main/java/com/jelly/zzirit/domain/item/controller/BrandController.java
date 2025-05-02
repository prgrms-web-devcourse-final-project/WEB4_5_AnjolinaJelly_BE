package com.jelly.zzirit.domain.item.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.BrandResponse;
import com.jelly.zzirit.global.dto.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/brands")
@Tag(name = "브랜드 API", description = "브랜드 관련 API를 설명합니다.")
public class BrandController {

	@GetMapping("/{type-id}")
	@Operation(summary = "상품 종류에 따른 브랜드 조회", description = "상품 종류 Id에 맞는 브랜드를 조회합니다.")
	public BaseResponse<List<BrandResponse>> findBrandByType(@PathVariable(name = "type-id") Long typeId) {
		return BaseResponse.success(
			List.of(new BrandResponse(1L, "삼성"))
		);
	}
}
