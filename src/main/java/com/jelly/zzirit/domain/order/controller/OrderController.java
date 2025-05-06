package com.jelly.zzirit.domain.order.controller;

import com.jelly.zzirit.domain.order.dto.request.OrderCreateRequest;
import com.jelly.zzirit.domain.order.dto.response.OrderFetchResponse;
import com.jelly.zzirit.domain.order.service.OrderCancellationFacade;
import com.jelly.zzirit.domain.order.service.QueryOrderService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.security.model.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "주문 API", description = "주문과 관련된 API를 설명합니다.")
public class OrderController {

    private final QueryOrderService queryOrderService;
    private final OrderCancellationFacade orderCancellationFacade;

    @PostMapping
    @Operation(summary = "주문 생성 API", description = "주문 하나를 생성합니다.")
    public BaseResponse<Empty> createOrder(@RequestBody OrderCreateRequest request) {
        return BaseResponse.success();
    }

    @GetMapping
    @Operation(summary = "주문 전체 조회 API", description = "전체 주문을 조회합니다.")
    public BaseResponse<List<OrderFetchResponse>> fetchAllOrders(@AuthenticationPrincipal MemberPrincipal member) {
        List<OrderFetchResponse> response = queryOrderService.findAllOrders(member.getMemberId())
            .stream().map(OrderFetchResponse::from).toList();

        return BaseResponse.success(response);
    }

    @DeleteMapping("/{order-id}")
    @Operation(summary = "주문 취소 API", description = "orderId에 해당되는 주문을 취소합니다.")
    public BaseResponse<Empty> cancelOrder(@PathVariable(name = "order-id") Long orderId) {
        orderCancellationFacade.cancelOrderAndRefund(
            orderId,
            AuthMember.getAuthUser()
        );

        return BaseResponse.success();
    }
}