package com.jelly.zzirit.domain.order.controller.pay;

import org.springframework.beans.factory.annotation.Autowired;

import com.jelly.zzirit.domain.order.service.order.CommandTempOrderService;
import com.jelly.zzirit.domain.order.service.pay.PaymentConfirmService;
import com.jelly.zzirit.domain.order.service.pay.PaymentInitService;
import com.jelly.zzirit.global.support.AcceptanceRabbitTest;

public class PaymentControllerTest extends AcceptanceRabbitTest {

	@Autowired
	private PaymentInitService paymentInitService;

	@Autowired
	private CommandTempOrderService commandTempOrderService;

	@Autowired
	private PaymentConfirmService paymentConfirmService;

}