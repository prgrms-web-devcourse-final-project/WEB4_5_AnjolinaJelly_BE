package com.jelly.zzirit.domain.order.controller.pay;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jelly.zzirit.domain.order.controller.PaymentController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jelly.zzirit.domain.order.service.order.TempOrderService;
import com.jelly.zzirit.domain.order.service.pay.TossConfirmService;
import com.jelly.zzirit.domain.order.service.pay.TossPaymentService;
import com.jelly.zzirit.global.support.TestMemberConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import({PaymentController.class, PaymentControllerTest.TestConfig.class})
class PaymentControllerTest extends TestMemberConfig {

	@Autowired private MockMvc mockMvc;
	@Autowired private TossPaymentService tossPaymentService;
	@Autowired private TossConfirmService tossConfirmService;
	@Autowired private TempOrderService tempOrderService;

	@TestConfiguration
	static class TestConfig {
		@Bean
		public TossPaymentService tossPaymentService() {
			return Mockito.mock(TossPaymentService.class);
		}

		@Bean
		public TossConfirmService tossConfirmService() {
			return Mockito.mock(TossConfirmService.class);
		}

		@Bean
		public TempOrderService tempOrderService() {
			return Mockito.mock(TempOrderService.class);
		}
	}

	@Test
	void 주문번호_생성_API는_쿠키인증으로_정상적으로_응답해야_한다() throws Exception {
		// given
		String mockOrderNumber = "ORDER-20250507-001";
		Mockito.when(tossPaymentService.createOrderAndReturnOrderNumber(Mockito.any()))
			.thenReturn(mockOrderNumber);

		String requestBody = """
            {
              "orderItems": [
                {
                  "itemId": 1,
                  "timeDealItemId": 100,
                  "quantity": 2,
                  "itemName": "샘플상품",
                  "price": 10000
                }
              ],
              "totalAmount": 20000,
              "shippingRequest": "문 앞에 두세요",
              "address": "서울특별시 강남구",
              "addressDetail": "101동 1001호"
            }
        """;

		// when & then
		mockMvc.perform(post("/api/payments/init")
				.cookie(getAccessTokenCookie())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.result").value(mockOrderNumber));

	}

	@Test
	void 결제성공_콜백_API는_쿠키인증으로_정상처리되어야_한다() throws Exception {
		mockMvc.perform(get("/api/payments/toss/success")
				.cookie(getAccessTokenCookie())
				.param("paymentKey", "pay_123")
				.param("orderId", "ORDER-123")
				.param("amount", "10000"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		Mockito.verify(tossConfirmService)
			.confirmPayment("pay_123", "ORDER-123", "10000");
	}

	@Test
	void 결제실패_콜백_API는_쿠키없어도_에러응답_반환한다() throws Exception {
		String code = "USER_CANCEL";
		String message = "사용자 취소";
		String orderId = "ORDER-123";

		mockMvc.perform(get("/api/payments/toss/fail")
				.cookie(getAccessTokenCookie())
				.param("code", code)
				.param("message", message)
				.param("orderId", orderId))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value(containsString("토스 결제 요청에 실패했습니다.")));

		Mockito.verify(tempOrderService)
			.deleteTempOrder(orderId, code, message);
	}
}