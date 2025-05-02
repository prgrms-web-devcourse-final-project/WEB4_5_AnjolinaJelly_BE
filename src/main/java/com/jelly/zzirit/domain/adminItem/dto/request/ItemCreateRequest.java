package com.jelly.zzirit.domain.adminItem.dto.request;

import lombok.Getter;

@Getter
public class ItemCreateRequest {
    private String name;
    private int stockQuantity;
    private int price;
    private Long typeId;
    private Long brandId;
}