package com.jelly.zzirit.domain.item.domain.fixture;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.TypeBrand;

public class ItemFixture {

	public static Item 삼성_노트북(TypeBrand typeBrand) {
		return Item.builder()
			.name("삼성 15인치 노트북")
			.price(BigDecimal.valueOf(1000000))
			.itemStatus(ItemStatus.NONE)
			.imageUrl("")
			.typeBrand(typeBrand)
			.build();
	}

	public static Item 삼성_노트북(ItemStatus itemStatus) {
		return Item.builder()
			.id(1L)
			.name("삼성 15인치 노트북")
			.price(BigDecimal.valueOf(1000000))
			.itemStatus(itemStatus)
			.imageUrl("")
			.typeBrand(new TypeBrand(new Type("노트북"), new Brand("삼성")))
			.build();
	}

	public static Item 상품_생성_이름(String name, TypeBrand typeBrand) {
		return Item.builder()
			.name(name)
			.price(BigDecimal.valueOf(1000000))
			.itemStatus(ItemStatus.NONE)
			.imageUrl("")
			.typeBrand(typeBrand)
			.build();
	}
}
