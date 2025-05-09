package com.jelly.zzirit.domain.item.domain.fixture;

import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.TypeBrand;

public class TypeBrandFixture {

	public static TypeBrand 타입_브랜드_생성(Type type, Brand brand) {
		return new TypeBrand(type, brand);
	}
}
