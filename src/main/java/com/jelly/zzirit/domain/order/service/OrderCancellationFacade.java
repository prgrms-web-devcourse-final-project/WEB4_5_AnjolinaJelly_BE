package com.jelly.zzirit.domain.order.service;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.service.order.CommandOrderService;
import com.jelly.zzirit.domain.order.service.pay.RefundService;
import com.jelly.zzirit.global.authorization.AuthorizationService;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancellationFacade {

    private final AuthorizationService authorizationService;
    private final RefundService refundService;
    private final CommandOrderService commandOrderService;
    private final OrderRepository orderRepository;

    /**
     * 주문 취소 및 결제 취소
     * @param orderId 취소할 주문의 아이디
     * @param member 주문 취소를 요청한 유저
     */
    public void cancelOrderAndRefund(Long orderId, Member member) {
        Order order = orderRepository.findByIdWithPayment(orderId)
            .orElseThrow(() -> new InvalidOrderException(ORDER_NOT_FOUND));

        // 주문 취소 권한 확인
        authorizationService.checkOrderCancelPermission(order, member);

        boolean isRefundSuccessful = true;

        try {
            // 트랜잭션 외부에서 결제 취소 API 호출
            refundService.refund(orderId, order.getPayment().getPaymentKey());
        } catch (Exception e) {
            isRefundSuccessful = false;
            log.error("환불 실패: orderId={}, error={}", orderId, e.getMessage(), e);
        } finally {
            // 트랜잭션 내부에서 주문 상태 및 결제 상태 변경
            commandOrderService.updateOrderAndPaymentStatusAfterRefund(orderId, isRefundSuccessful);
        }

        // 결제 취소에 실패한 경우에만 예외 발생
        if (!isRefundSuccessful) {
            throw new InvalidOrderException(TOSS_REFUND_FAILED);
        }
    }
}
