package com.jelly.zzirit.domain.order.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

	@Bean
	public Map<PaymentProvider, PaymentGateway> paymentGatewayMap(List<PaymentGateway> gateways) {
		return gateways.stream()
			.collect(Collectors.toMap(PaymentGateway::getPaymentProvider, Function.identity()));
	}
}