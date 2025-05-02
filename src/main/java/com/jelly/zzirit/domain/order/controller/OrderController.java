package com.jelly.zzirit.domain.order.controller;

import com.jelly.zzirit.domain.order.dto.request.OrderCreateRequest;
import com.jelly.zzirit.domain.order.dto.response.OrderFetchResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.orderitem.dto.response.OrderItemFetchResponse;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "주문 API", description = "주문과 관련된 API를 설명합니다.")
public class OrderController {

    @PostMapping
    @Operation(summary = "주문 생성 API", description = "주문 하나를 생성합니다.")
    public BaseResponse<Empty> createOrder(@RequestBody OrderCreateRequest request) {
        return BaseResponse.success();
    }

    @GetMapping
    @Operation(summary = "주문 전체 조회 API", description = "전체 주문을 조회합니다.")
    public BaseResponse<List<OrderFetchResponse>> fetchAllOrders() {
        List<OrderFetchResponse> orders = List.of(
            new OrderFetchResponse(
                LocalDateTime.now().minusDays(1),
                1L,
                UUID.randomUUID().toString(),
                new BigDecimal("25000"),
                Order.OrderStatus.COMPLETED,
                List.of(
                    new OrderItemFetchResponse("아메리카노", 2, "https://example.com/img1.jpg", new BigDecimal("10000")),
                    new OrderItemFetchResponse("카페라떼", 1, "https://example.com/img2.jpg", new BigDecimal("15000"))
                )
            ),
                new OrderFetchResponse(
                    LocalDateTime.now().minusDays(3),
                    2L,
                    UUID.randomUUID().toString(),
                    new BigDecimal("18000"),
                    Order.OrderStatus.COMPLETED,
                    List.of(
                        new OrderItemFetchResponse("콜드브루", 1, "https://example.com/img3.jpg", new BigDecimal("6000")),
                        new OrderItemFetchResponse("카라멜마끼아또", 1, "https://example.com/img4.jpg", new BigDecimal("12000"))
                    )
                )
        );

        return BaseResponse.success(orders);
    }

    @DeleteMapping("/{order-id}")
    @Operation(summary = "주문 취소 API", description = "orderId에 해당되는 주문을 취소합니다.")
    public BaseResponse<Empty> cancelOrder(@PathVariable(name = "order-id") Long orderId) {
        return BaseResponse.success();
    }

}
