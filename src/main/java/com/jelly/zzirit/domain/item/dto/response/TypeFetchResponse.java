package com.jelly.zzirit.domain.item.dto.response;

import com.jelly.zzirit.domain.item.entity.Type;

public record TypeFetchResponse(
	Long typeId,
	String name
) {

	public static TypeFetchResponse from(Type type) {
		return new TypeFetchResponse(type.getId(), type.getName());
	}
}
