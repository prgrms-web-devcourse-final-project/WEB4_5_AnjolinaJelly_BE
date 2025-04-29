package com.jelly.zzirit.product.dto.response;

public record ProductResponse (
	Long id,
	String name,
	String image,
	Integer stock,
	Integer price
) {
}
