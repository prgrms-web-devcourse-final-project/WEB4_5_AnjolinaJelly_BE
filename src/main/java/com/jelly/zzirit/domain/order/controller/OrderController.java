package com.jelly.zzirit.domain.order.controller;

import com.jelly.zzirit.domain.order.dto.response.OrderFetchResponse;
import com.jelly.zzirit.domain.order.service.OrderCancellationFacade;
import com.jelly.zzirit.domain.order.service.QueryOrderService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "주문 API", description = "주문과 관련된 API를 설명합니다.")
public class OrderController {

    private final QueryOrderService queryOrderService;
    private final OrderCancellationFacade orderCancellationFacade;

    @GetMapping
    @Operation(summary = "주문 전체 조회 API", description = "전체 주문을 페이징 처리하여 최신순으로 조회합니다.")
    public BaseResponse<PageResponse<OrderFetchResponse>> fetchAllOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
        ) {
        Long memberId = AuthMember.getMemberId();
        Pageable pageable = PageRequest.of(page, size);

        return BaseResponse.success(PageResponse.from(
            queryOrderService.findPagedOrders(memberId, pageable)
                .map(OrderFetchResponse::from)
        ));
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