package com.jelly.zzirit.domain.order.service;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.service.order.CommandOrderService;
import com.jelly.zzirit.domain.order.service.order.OrderCancelValidator;
import com.jelly.zzirit.domain.order.service.pay.CommandRefundService;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancellationFacade {

    private final OrderCancelValidator orderCancelValidator;
    private final CommandRefundService refundService;
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

        // 주문 취소 가능 여부 검증
        orderCancelValidator.validate(order, member);

        // 트랜잭션 외부에서 결제 취소 API 호출
        try {
            refundService.refund(order, order.getPayment().getPaymentKey(), "사용자 주문 취소");
        } catch (InvalidOrderException e) {
            log.error("환불 실패로 주문 취소가 완료되지 않았습니다. orderId={}", orderId, e);
            throw e; // 환불 실패 시 예외 재던져서 취소 로직을 중단합니다.
        }

        // 트랜잭션 내부에서 주문 상태 및 결제 상태 변경
        commandOrderService.applyRefundResult(orderId, true);

        log.info("주문이 취소되었습니다. orderId={}", orderId);
    }
}