package com.jelly.zzirit.domain.cart.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 선택 삭제 요청 DTO")
public record CartItemDeleteRequest(
	List<Long> itemIds
) {}