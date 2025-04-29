package com.jelly.zzirit.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude
public final class Empty {
	private static final Empty INSTANCE = new Empty();

	@SuppressWarnings("unused")
	private final boolean isEmpty = true; // 유틸리티 클래스가 아님을 명확하게 하기 위한 변수

	private Empty() {}

	public static Empty getInstance() {
		return INSTANCE;
	}
}