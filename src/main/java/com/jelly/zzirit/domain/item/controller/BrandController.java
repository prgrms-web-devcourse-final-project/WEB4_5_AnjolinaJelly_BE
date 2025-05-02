package com.jelly.zzirit.domain.item.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.BrandResponse;
import com.jelly.zzirit.global.dto.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/brands")
public class BrandController {

	@GetMapping("/{type-id}")
	@Operation
	public BaseResponse<List<BrandResponse>> findBrandByType(@PathVariable(name = "type-id") Long typeId) {
		return BaseResponse.success(
			List.of(new BrandResponse(1L, "삼성"))
		);
	}
}
