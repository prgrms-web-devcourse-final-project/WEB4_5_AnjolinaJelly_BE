package com.jelly.zzirit.domain.order.controller.pay;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.*;
import static io.restassured.RestAssured.given;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import java.util.List;

import com.jelly.zzirit.domain.order.dto.request.OrderItemCreateRequest;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.dto.response.PaymentConfirmResponse;
import com.jelly.zzirit.domain.order.dto.response.PaymentInitResponse;
import com.jelly.zzirit.domain.order.service.order.manage.CommandTempOrderService;
import com.jelly.zzirit.domain.order.service.pay.CommandPaymentConfirmService;
import com.jelly.zzirit.domain.order.service.pay.CommandPaymentInitService;
import com.jelly.zzirit.global.support.AcceptanceTest;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PaymentControllerTest extends AcceptanceTest {

	@MockitoBean
	private CommandPaymentInitService commandPaymentInitService;

	@MockitoBean
	private CommandPaymentConfirmService commandPaymentConfirmService;

	@MockitoBean
	private CommandTempOrderService commandTempOrderService;

	@Test
	@DisplayName("주문번호 생성 성공 시 200 응답")
	void 결제_초기화_성공() {
		// given
		PaymentRequest 요청 = new PaymentRequest(
			List.of(new OrderItemCreateRequest(1L, "모나미 볼펜", 2)),
			15000,
			"문 앞에 놔주세요",
			"서울 종로구 종로1길 10",
			"101동 202호"
		);

		given(commandPaymentInitService.createOrderAndReturnInit(any()))
			.willReturn(new PaymentInitResponse("ORD20240516-000001", 15000, "모나미 볼펜 외 1건", "홍길동"));

		RequestSpecification 요청사양 = given(spec)
			.cookie(getCookie())
			.filter(문서_결제초기화("결제 초기화"))
			.contentType("application/json")
			.body(요청);

		// when
		Response 응답 = 요청사양.post("/api/payments/init");

		// then
		응답.then().log().body().statusCode(200);
	}

	@Test
	@DisplayName("결제 성공 시 200 응답")
	void 결제_성공_확정_응답() {
		// given
		String orderId = "ORD123";
		String paymentKey = "pay_12345";
		String amount = "15000";

		given(commandPaymentConfirmService.confirmPayment(any(), any(), any()))
			.willReturn(new PaymentConfirmResponse(orderId, paymentKey, 15000));

		RequestSpecification 요청 = given(spec)
			.cookie(getCookie())
			.filter(문서_결제성공("결제 성공"))
			.queryParam("paymentKey", paymentKey)
			.queryParam("orderId", orderId)
			.queryParam("amount", amount);

		// when
		Response 응답 = 요청.get("/api/payments/success");

		// then
		응답.then().log().body().statusCode(200);
	}

	@Test
	@DisplayName("결제 실패 시 400 응답")
	void 결제_실패_응답() {
		// given
		String code = "4000";
		String message = "사용자 취소";
		String orderId = "ORD123";

		RequestSpecification 요청 = given(spec)
			.cookie(getCookie())
			.filter(문서_결제실패("결제 실패", "결제 실패", "결제 실패 또는 사용자 취소 시 임시 주문이 삭제됩니다."))
			.queryParam("code", code)
			.queryParam("message", message)
			.queryParam("orderId", orderId);

		// when
		Response 응답 = 요청.get("/api/payments/fail");

		// then
		응답.then().log().body().statusCode(400);
	}

	private RestDocumentationFilter 문서_결제초기화(String name) {
		return document(
			name,
			resourceDetails()
				.summary("결제 초기화")
				.description("결제를 위한 주문번호를 생성하고 임시 주문을 생성합니다."),
			requestFields(
				fieldWithPath("orderItems[].itemId").description("상품 ID").type(NUMBER),
				fieldWithPath("orderItems[].itemName").description("상품 이름").type(STRING),
				fieldWithPath("orderItems[].quantity").description("주문 수량").type(NUMBER),
				fieldWithPath("totalAmount").description("총 결제 금액").type(NUMBER),
				fieldWithPath("shippingRequest").description("배송 요청사항").type(STRING),
				fieldWithPath("address").description("기본 주소").type(STRING),
				fieldWithPath("addressDetail").description("상세 주소").type(STRING)
			),
			responseFields(
				fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
				fieldWithPath("code").description("커스텀 응답 코드").type(NUMBER),
				fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
				fieldWithPath("message").description("응답 메시지").type(STRING),
				fieldWithPath("result.orderId").description("주문 ID").type(STRING),
				fieldWithPath("result.amount").description("결제 금액").type(NUMBER),
				fieldWithPath("result.orderName").description("주문명").type(STRING),
				fieldWithPath("result.customerName").description("구매자 이름").type(STRING)
			)
		);
	}

	private RestDocumentationFilter 문서_결제성공(String name) {
		return document(
			name,
			resourceDetails()
				.summary(name)
				.description("결제 완료 시 결제 확정 처리"),
			queryParameters(
				parameterWithName("paymentKey").description("결제 키"),
				parameterWithName("orderId").description("주문 ID"),
				parameterWithName("amount").description("결제 금액")
			),
			responseFields(
				fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
				fieldWithPath("code").description("커스텀 응답 코드").type(NUMBER),
				fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
				fieldWithPath("message").description("응답 메시지").type(STRING),
				fieldWithPath("result.orderId").description("주문번호").type(STRING),
				fieldWithPath("result.paymentKey").description("결제 키").type(STRING),
				fieldWithPath("result.amount").description("결제 금액").type(NUMBER)
			)
		);
	}

	private RestDocumentationFilter 문서_결제실패(String name, String summary, String description) {
		return document(
			name,
			resourceDetails()
				.summary(summary)
				.description(description),
			queryParameters(
				parameterWithName("code").description("에러 코드"),
				parameterWithName("message").description("에러 메시지"),
				parameterWithName("orderId").description("주문 ID")
			),
			responseFields(
				fieldWithPath("success").description("요청 성공 여부").type(BOOLEAN),
				fieldWithPath("code").description("커스텀 응답 코드").type(NUMBER),
				fieldWithPath("httpStatus").description("HTTP 상태 코드").type(NUMBER),
				fieldWithPath("message").description("에러 메시지").type(STRING),
				fieldWithPath("result").description("빈 응답").type(OBJECT)
			)
		);
	}
}

