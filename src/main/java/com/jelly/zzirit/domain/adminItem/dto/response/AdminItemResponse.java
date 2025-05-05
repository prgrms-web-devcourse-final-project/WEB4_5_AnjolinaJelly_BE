package com.jelly.zzirit.domain.adminItem.dto.response;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import lombok.Builder;

@Builder // record는 getter, allagrsconstructor 자동 생성
public record AdminItemResponse (
        Long id, // record는 다 private final
        String name,
        String imageUrl,
        String type,
        String brand,
        int price,
        int stockQuantity
){
    public static AdminItemResponse from (Item item, ItemStock itemStock) { // item에 해당하는 itemStock을 조회해서 넣어야함(단방향이라서)
        return AdminItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .imageUrl(item.getImageUrl())
                .type(item.getTypeBrand().getType().getName()) // TODO: enum으로 관리?
                .brand(item.getTypeBrand().getBrand().getName())
                .price(item.getPrice().intValue()) // TODO: 추후 bigdecimal로 스펙 변경 필요
                .stockQuantity(itemStock.getQuantity() - itemStock.getSoldQuantity()) // Todo: 재고수량 둘 다 줄지? 상품-재고 분리?(재고는 캐시x?), 단방향(양방향 할거면 합치기?)
                .build();
    }
}
