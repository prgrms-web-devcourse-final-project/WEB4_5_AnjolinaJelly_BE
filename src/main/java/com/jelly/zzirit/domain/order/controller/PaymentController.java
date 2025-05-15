package com.jelly.zzirit.domain.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.dto.response.PaymentConfirmResponse;
import com.jelly.zzirit.domain.order.dto.response.PaymentInitResponse;
import com.jelly.zzirit.domain.order.service.order.CommandTempOrderService;
import com.jelly.zzirit.domain.order.service.pay.PaymentConfirmService;
import com.jelly.zzirit.domain.order.service.pay.PaymentInitService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "결제(Payments)", description = "토스 페이먼츠 결제 관련 API")
@SecurityRequirement(name = "accessToken")
public class PaymentController {

	private final PaymentInitService paymentInitService;
	private final CommandTempOrderService commandTempOrderService;
	private final PaymentConfirmService paymentConfirmService;

	@Operation(
		summary = "주문번호 생성",
		description = "결제를 위한 주문번호를 생성하고 임시 주문을 저장합니다."
	)
	@PostMapping("/init")
	public BaseResponse<PaymentInitResponse> initOrder(@RequestBody @Valid PaymentRequest requestDto) {
		PaymentInitResponse initResponse = paymentInitService.createOrderAndReturnInit(requestDto);
		return BaseResponse.success(initResponse);
	}

	@Operation(
		summary = "결제 성공",
		description = "결제 성공 시 주문을 확정 처리합니다."
	)
	@GetMapping("/success")
	public BaseResponse<PaymentConfirmResponse> confirmPayment(
		@RequestParam("paymentKey") String paymentKey,
		@RequestParam("orderId") String orderId,
		@RequestParam("amount") String amount
	) {
		PaymentConfirmResponse response = paymentConfirmService.confirmPayment(paymentKey, orderId, amount);
		return BaseResponse.success(response);
	}

	@Operation(
		summary = "결제 실패",
		description = "결제 실패 또는 사용자 취소 시 임시 주문이 삭제됩니다."
	)
	@GetMapping("/fail")
	public BaseResponse<String> failPayment(
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String message,
		@RequestParam(required = false) String orderId
	) {
		commandTempOrderService.deleteTempOrder(orderId);
		String failReason = String.format("결제 실패 (%s): %s | 주문번호: %s", code, message, orderId);
		return BaseResponse.error(BaseResponseStatus.TOSS_PAYMENT_REQUEST_FAILED, failReason);
	}
}