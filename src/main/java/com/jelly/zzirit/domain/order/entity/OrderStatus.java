package com.jelly.zzirit.domain.order.entity;

public enum OrderStatus {
    PENDING, // 결제 완료 전
    PAID, // 결제 완료 // 주문 취소 가능한 상태
    FAILED, // 실패
    CANCELLED, // 주문 취소
    COMPLETED // 주문 완료 // 24시간 이후의 주문이므로 주문 취소 불가능한 상태
}
