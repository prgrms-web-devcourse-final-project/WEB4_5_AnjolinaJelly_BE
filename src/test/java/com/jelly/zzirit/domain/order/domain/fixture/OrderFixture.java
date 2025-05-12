package com.jelly.zzirit.domain.order.domain.fixture;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Order.OrderStatus;
import com.jelly.zzirit.domain.order.entity.Payment;

import static com.jelly.zzirit.domain.order.domain.fixture.PaymentFixture.결제_정보_생성;

public class OrderFixture {

    public static Order 주문_생성(Member member) {
        return Order.builder()
            .member(member)
            .totalPrice(BigDecimal.valueOf(10000))
            .status(OrderStatus.PAID)
            .shippingRequest("문 앞에 놔주세요")
            .orderNumber(generateUniqueOrderNumber())
            .address("서울시 강남구 테헤란로 123")
            .addressDetail("101호")
            .build();
    }

    public static Order 결제된_주문_생성(Member member) {
        Order order = 주문_생성(member);

        Payment payment = 결제_정보_생성(order);
        order.addPayment(payment);

        return order;
    }

    private static String generateUniqueOrderNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String timestamp = sdf.format(new Date());

        return "order-" + timestamp + "-" + (System.currentTimeMillis() % 1000000);  // 밀리초 단위로 유니크 값 추가
    }
}
