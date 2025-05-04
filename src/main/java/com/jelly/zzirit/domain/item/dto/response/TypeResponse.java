package com.jelly.zzirit.domain.item.dto.response;

import com.jelly.zzirit.domain.item.entity.Type;

public record TypeResponse(
	Long typeId,
	String name
) {

	public static TypeResponse from(Type type) {
		return new TypeResponse(type.getId(), type.getName());
	}
}
