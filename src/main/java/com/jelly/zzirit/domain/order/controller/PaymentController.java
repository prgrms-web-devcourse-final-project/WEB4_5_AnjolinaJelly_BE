package com.jelly.zzirit.domain.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.order.dto.request.PaymentRequestDto;
import com.jelly.zzirit.domain.order.service.pay.TossConfirmService;
import com.jelly.zzirit.domain.order.service.pay.TossPaymentService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;

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

	private final TossPaymentService tossPaymentService;
	private final TossConfirmService tossConfirmService;

	@Operation(
		summary = "주문번호 생성",
		description = "결제를 위한 주문번호를 생성하고 임시 주문을 저장합니다."
	)
	@PostMapping("/init")
	public BaseResponse<String> initOrder(@RequestBody @Valid PaymentRequestDto requestDto) {
		String orderNumber = tossPaymentService.createOrderAndReturnOrderNumber(requestDto);
		return BaseResponse.success(orderNumber);
	}

	@Operation(
		summary = "결제 성공 콜백",
		description = "결제 성공 시 Toss 에서 호출하는 콜백입니다. 주문을 확정 처리합니다."
	)
	@GetMapping("/toss/success")
	public BaseResponse<Empty> confirmPayment(
		@RequestParam("paymentKey") String paymentKey,
		@RequestParam("orderId") String orderId,
		@RequestParam("amount") String amount
	) {
		tossConfirmService.confirmPayment(paymentKey, orderId, amount);
		return BaseResponse.success();
	}

	@Operation(
		summary = "결제 실패 콜백",
		description = "결제 실패 또는 사용자 취소 시 Toss 에서 호출하는 콜백입니다."
	)
	@GetMapping("/toss/fail")
	public BaseResponse<String> failPayment(
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String message,
		@RequestParam(required = false) String orderId
	) {
		String failReason = String.format("결제 실패 (%s): %s | 주문번호: %s", code, message, orderId);
		return BaseResponse.error(BaseResponseStatus.TOSS_PAYMENT_REQUEST_FAILED, failReason);
	}
}