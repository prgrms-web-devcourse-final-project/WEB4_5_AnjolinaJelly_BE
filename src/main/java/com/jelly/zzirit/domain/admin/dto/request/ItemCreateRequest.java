package com.jelly.zzirit.domain.admin.dto.request;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ItemCreateRequest (
    @NotBlank String name, // validity check
    @PositiveOrZero Integer stockQuantity,
    @PositiveOrZero BigDecimal price,
    @NotNull Long typeId,
    @NotNull Long brandId,
    @NotBlank String imageUrl
){
    public Item toItemEntity (TypeBrand typeBrand) { // 서비스에서 주입, 외부 사용 public
        return Item.builder()
                .name(name)
                .imageUrl(imageUrl)
                .price(price)
                .typeBrand(typeBrand)
                .itemStatus(ItemStatus.NONE)
                .build();
    }

    public ItemStock toItemStockEntity (Item item) {
        return ItemStock.builder()
            .item(item)
            .quantity(stockQuantity)
            .build();
    }
}