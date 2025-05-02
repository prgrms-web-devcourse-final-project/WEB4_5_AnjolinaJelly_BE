package com.jelly.zzirit.domain.order.controller;

import com.jelly.zzirit.domain.order.dto.request.OrderCreateRequest;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "주문 API", description = "주문과 관련된 API를 설명합니다.")
public class OrderController {

    @PostMapping
    @Operation(summary = "주문 생성 API", description = "주문 하나를 생성합니다.")
    public BaseResponse<Empty> createOrder(@RequestBody OrderCreateRequest request) {
        return BaseResponse.success();
    }

}
