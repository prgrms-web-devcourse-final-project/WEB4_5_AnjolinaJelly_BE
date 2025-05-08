package com.jelly.zzirit.domain.item.domain.fixture;

import com.jelly.zzirit.domain.item.entity.Brand;

public class BrandFixture {

	public static Brand 삼성() {
		return new Brand("삼성");
	}

	public static Brand 브랜드_생성(String name) {
		return new Brand(name);
	}
}
