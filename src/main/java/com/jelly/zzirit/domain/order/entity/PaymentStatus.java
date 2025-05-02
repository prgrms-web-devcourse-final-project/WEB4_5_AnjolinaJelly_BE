package com.jelly.zzirit.domain.order.entity;

public enum PaymentStatus {
	READY,      // 결제 대기
	PAID,       // 결제 완료
	FAILED,     // 결제 실패
	CANCELLED,  // 사용자 취소
	REFUNDED    // 환불 완료
}