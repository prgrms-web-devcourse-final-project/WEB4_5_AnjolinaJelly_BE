package com.jelly.zzirit.domain.adminItem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminItemResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private int stockQuantity;
    private String type;   // ex. 노트북
    private String brand;  // ex. 삼성
    private int price;
}
