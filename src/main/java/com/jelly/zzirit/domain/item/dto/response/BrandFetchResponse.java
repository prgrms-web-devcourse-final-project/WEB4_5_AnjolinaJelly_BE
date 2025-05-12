package com.jelly.zzirit.domain.item.dto.response;

import com.jelly.zzirit.domain.item.entity.Brand;

public record BrandFetchResponse(
	Long brandId,
	String name
) {

	public static BrandFetchResponse from(Brand brand) {
		return new BrandFetchResponse(brand.getId(), brand.getName());
	}
}
