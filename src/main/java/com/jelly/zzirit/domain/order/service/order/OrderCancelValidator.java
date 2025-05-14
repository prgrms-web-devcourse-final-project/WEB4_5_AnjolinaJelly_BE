package com.jelly.zzirit.domain.order.service.order;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@Service
public class OrderCancelValidator {

    /**
     * 주문 취소 가능 여부 검증
     * @param order 취소할 주문
     * @param member 주문 취소를 요청한 유저
     */
    public void validate(Order order, Member member) {
        checkPermission(order, member);
        checkCancellable(order);
    }

    private void checkPermission(Order order, Member member) {
        boolean isAdmin = (member.getRole() == Role.ROLE_ADMIN);

        // 관리자나 주문을 접수한 유저만 주문 취소 가능
        if (!isAdmin && !order.wasOrderedBy(member.getId())) {
            throw new InvalidOrderException(ACCESS_DENIED);
        }
    }

    private void checkCancellable(Order order) {
        // 취소 가능한 주문인지 확인
        order.validateCancellable();
    }

}
