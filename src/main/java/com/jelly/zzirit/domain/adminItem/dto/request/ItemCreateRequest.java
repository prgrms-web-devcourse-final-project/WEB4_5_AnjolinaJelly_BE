package com.jelly.zzirit.domain.adminItem.dto.request;

import com.jelly.zzirit.domain.item.entity.*;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ItemCreateRequest (
        @NotBlank String name, // validity check
        @PositiveOrZero int stockQuantity,
        @PositiveOrZero int price,
        @NotNull Long typeId,
        @NotNull Long brandId
){
    public Item toItemEntity (TypeBrand typeBrand) { // 서비스에서 주입, 외부 사용 public
        return Item.builder()
                .name(name)
                .price(BigDecimal.valueOf(price)) // todo: int->bigdecimal로 변경 필요
                .typeBrand(typeBrand)
                .itemStatus(ItemStatus.NONE) // todo: item status 업데이트 로직 추가
                .build();
    }

    public ItemStock toItemStockEntity (Item item) {
        return ItemStock.builder()
                .item(item)
                .quantity(stockQuantity)
                .build();
    }
}