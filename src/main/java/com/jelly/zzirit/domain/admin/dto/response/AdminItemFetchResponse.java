package com.jelly.zzirit.domain.admin.dto.response;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AdminItemFetchResponse(
    Long id,
    String name,
    String imageUrl,
    String type,
    String brand,
    BigDecimal price,
    int stockQuantity
) {

    public static AdminItemFetchResponse from (Item item, ItemStock itemStock) {
        return AdminItemFetchResponse.builder()
            .id(item.getId())
            .name(item.getName())
            .imageUrl(item.getImageUrl())
            .type(item.getTypeBrand().getType().getName())
            .brand(item.getTypeBrand().getBrand().getName())
            .price(item.getPrice())
            .stockQuantity(itemStock.getQuantity())
            .build();
    }
}