package com.jelly.zzirit.domain.order.util;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentGatewayResolver {

	private final Map<PaymentProvider, PaymentGateway> gatewayMap;

	public PaymentGateway resolve(PaymentProvider provider) {
		PaymentGateway gateway = gatewayMap.get(provider);
		if (gateway == null) {
			throw new InvalidOrderException(BaseResponseStatus.UNREGISTERED_PAYMENT_GATEWAY);
		}
		return gateway;
	}
}