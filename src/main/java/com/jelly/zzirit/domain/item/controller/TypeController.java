package com.jelly.zzirit.domain.item.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.TypeResponse;
import com.jelly.zzirit.global.dto.BaseResponse;

@RestController
@RequestMapping("/api/types")
public class TypeController {

	@GetMapping
	public BaseResponse<List<TypeResponse>> findType() {
		return BaseResponse.success(
			List.of(new TypeResponse(1L, "노트북"))
		);
	}
}
