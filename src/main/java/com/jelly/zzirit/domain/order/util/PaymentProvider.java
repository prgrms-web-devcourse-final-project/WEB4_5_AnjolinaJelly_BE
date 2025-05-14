package com.jelly.zzirit.domain.order.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentProvider {

	NONE("none"),
	TOSS("toss"),
	NAVER("naver");

	private final String value;

	@JsonValue
	public String getValue() {
		return value;
	}

	@JsonCreator
	public static PaymentProvider from(String value) {
		for (PaymentProvider provider : values()) {
			if (provider.value.equalsIgnoreCase(value)) {
				return provider;
			}
		}
		throw new InvalidOrderException(BaseResponseStatus.UNSUPPORTED_PAYMENT_PROVIDER);
	}
}