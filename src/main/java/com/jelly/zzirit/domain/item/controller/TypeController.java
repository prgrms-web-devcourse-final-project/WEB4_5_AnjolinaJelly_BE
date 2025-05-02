package com.jelly.zzirit.domain.item.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.TypeResponse;
import com.jelly.zzirit.global.dto.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/types")
@Tag(name = "상품 종류 API", description = "상품 종류와 관련된 API를 설명합니다.")
public class TypeController {

	@GetMapping
	@Operation(summary = "상품 종류 전체 조회", description = "상품 종류를 전체 조회 합니다.")
	public BaseResponse<List<TypeResponse>> findType() {
		return BaseResponse.success(
			List.of(new TypeResponse(1L, "노트북"))
		);
	}
}
