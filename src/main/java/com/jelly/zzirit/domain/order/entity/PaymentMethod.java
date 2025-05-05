package com.jelly.zzirit.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

	CARD("카드"),
	VIRTUAL_ACCOUNT("가상계좌"),
	ACCOUNT_TRANSFER("계좌이체"),
	MOBILE_PHONE("휴대폰"),
	EASY_PAY("간편결제");

	private final String tossName;

	public static PaymentMethod from(String tossValue) {
		for (PaymentMethod method : values()) {
			if (method.tossName.equalsIgnoreCase(tossValue)) {
				return method;
			}
		}
		throw new IllegalArgumentException("지원하지 않는 결제 수단: " + tossValue);
	}
}