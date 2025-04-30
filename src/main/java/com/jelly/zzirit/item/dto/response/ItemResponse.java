package com.jelly.zzirit.item.dto.response;

public record ItemResponse(
	Long id,
	String name,
	String image,
	Integer stock,
	Integer price
) {
}
