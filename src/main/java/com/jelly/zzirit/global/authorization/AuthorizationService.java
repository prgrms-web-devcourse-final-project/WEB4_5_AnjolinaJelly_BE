package com.jelly.zzirit.global.authorization;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@Service
public class AuthorizationService {

    /**
     * 주문 취소 권한 확인
     * @param order 취소할 주문
     * @param member 주문 취소를 요청한 유저
     */
    public void checkOrderCancelPermission(Order order, Member member) {
        boolean isAdmin = (member.getRole() == Role.ROLE_ADMIN);

        // 관리자나 주문을 접수한 유저만 주문 취소 가능
        if (!isAdmin && !order.isOwnedBy(member.getId())) {
            throw new InvalidOrderException(ACCESS_DENIED);
        }

        // 취소 가능한 주문인지 확인
        order.checkCancellation();
    }

}
