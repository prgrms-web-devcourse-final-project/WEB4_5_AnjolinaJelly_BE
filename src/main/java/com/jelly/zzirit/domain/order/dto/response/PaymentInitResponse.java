package com.jelly.zzirit.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "결제 초기화 응답 DTO")
public class PaymentInitResponse {

	@Schema(description = "결제 제공자", example = "toss")
	private String provider;

	@Schema(description = "SDK 스크립트 URL", example = "https://js.tosspayments.com/v1")
	private String sdkUrl;

	@Schema(description = "SDK 글로벌 객체명", example = "TossPayments")
	private String sdkGlobal;

	@Schema(description = "결제 모듈 클라이언트 키", example = "test_ck_xxxxxxxxxxxxx")
	private String clientKey;

	@Schema(description = "주문 ID", example = "ORD20240514-000001")
	private String orderId;

	@Schema(description = "결제 금액 (단위: 원)", example = "15000")
	private int amount;

	@Schema(description = "주문명 (프론트 결제창 표시용)")
	private String orderName;

	@Schema(description = "구매자 이름", example = "홍길동")
	private String customerName;

	@Schema(description = "결제 성공 시 리디렉션 URL")
	private String successUrl;

	@Schema(description = "결제 실패 시 리디렉션 URL")
	private String failUrl;
}