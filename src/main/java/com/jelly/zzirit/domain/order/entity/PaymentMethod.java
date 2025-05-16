package com.jelly.zzirit.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

	NONE("", ""),
	CARD("CARD", "카드"),
	VIRTUAL_ACCOUNT("VIRTUAL_ACCOUNT", "가상계좌"),
	ACCOUNT_TRANSFER("TRANSFER", "계좌이체"),
	MOBILE_PHONE("MOBILE_PHONE", "휴대폰"),
	EASY_PAY("EASY_PAY", "간편결제");

	private final String code;
	private final String display;

	public static PaymentMethod from(String tossCode) {
		for (PaymentMethod method : values()) {
			if (method.code.equalsIgnoreCase(tossCode)) {
				return method;
			}
		}
		throw new IllegalArgumentException("지원하지 않는 결제 수단 코드: " + tossCode);
	}
}