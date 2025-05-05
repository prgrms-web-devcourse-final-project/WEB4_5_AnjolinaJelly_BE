package com.jelly.zzirit.domain.order.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequestDto;
import com.jelly.zzirit.domain.order.service.CommandOrderService;
import com.jelly.zzirit.domain.order.service.TossPaymentService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "결제(Payments)", description = "토스 페이먼츠 결제 관련 API")
@SecurityRequirement(name = "accessToken")
public class PaymentController {

	private final TossPaymentService tossPaymentService;
	private final CommandOrderService commandOrderService;

	@Operation(
		summary = "결제 요청",
		description = "주문 정보를 기반으로 Toss 결제 URL 을 생성합니다."
	)
	@PostMapping
	public BaseResponse<String> requestPayment(@RequestBody @Valid PaymentRequestDto requestDto) {
		String paymentUrl = tossPaymentService.createPayment(requestDto);
		return BaseResponse.success(paymentUrl);
	} // 프론트는 이 URL 로 window.location.href

	@Operation(
		summary = "결제 성공 콜백",
		description = "결제 성공 시 Toss 에서 호출하는 콜백입니다. 주문을 확정 처리합니다."
	)
	@GetMapping("/toss/success")
	public BaseResponse<Empty> confirmPayment(
		@RequestParam String paymentKey,
		@RequestParam String orderId,
		@RequestParam BigDecimal amount
	) {
		log.info("결제 성공 콜백: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
		commandOrderService.confirmPayment(orderId);
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
		log.warn("결제 실패: code={}, message={}, orderId={}", code, message, orderId);

		String failReason = String.format("결제 실패 (%s): %s", code, message);
		return BaseResponse.error(BaseResponseStatus.TOSS_PAYMENT_REQUEST_FAILED, failReason);
	}
}