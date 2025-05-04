package com.jelly.zzirit.domain.item.dto.response;

import com.jelly.zzirit.domain.item.entity.Brand;

public record BrandResponse (
	Long brandId,
	String name
) {

	public static BrandResponse from(Brand brand) {
		return new BrandResponse(brand.getId(), brand.getName());
	}
}
