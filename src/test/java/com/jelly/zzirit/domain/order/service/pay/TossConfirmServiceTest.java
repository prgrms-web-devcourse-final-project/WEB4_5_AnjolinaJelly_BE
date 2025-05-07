package com.jelly.zzirit.domain.order.service.pay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.order.dto.response.TossPaymentResponse;
import com.jelly.zzirit.domain.order.service.order.TempOrderService;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class TossConfirmServiceTest {

	@InjectMocks
	private TossConfirmService tossConfirmService;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private TempOrderService tempOrderService;

	@Test
	void 정상적인_요청이면_tempOrderService가_호출된다() throws Exception {
		// given
		String paymentKey = "pay_123";
		String orderId = "ORDER-001";
		String amount = "10000";
		String responseBody = "{\"status\": \"DONE\"}";

		ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(responseBody);
		TossPaymentResponse mockResponse = mock(TossPaymentResponse.class);

		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
			.thenReturn(mockResponseEntity);
		when(objectMapper.readValue(responseBody, TossPaymentResponse.class))
			.thenReturn(mockResponse);

		// when
		tossConfirmService.confirmPayment(paymentKey, orderId, amount);

		// then
		verify(tempOrderService).confirmTempOrder(paymentKey, orderId, amount, mockResponse);
	}

	@Test
	void 응답변환중_예외가_발생하면_예외처리된다() throws Exception {
		// given
		String paymentKey = "pay_404";
		String responseBody = "INVALID_JSON";

		ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(responseBody);
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
			.thenReturn(mockResponseEntity);

		when(objectMapper.readValue(anyString(), eq(TossPaymentResponse.class)))
			.thenAnswer(invocation -> { throw new IOException("JSON 파싱 실패"); });

		// when & then
		assertThrows(InvalidOrderException.class, () ->
			tossConfirmService.confirmPayment(paymentKey, "ORDER-001", "10000"));
	}


	@Test
	void 요청이_실패하면_예외가_발생한다() {
		// given
		String paymentKey = "pay_123";
		String orderId = "ORDER-001";
		String amount = "10000";

		when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
			.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

		// when & then
		assertThrows(InvalidOrderException.class, () ->
			tossConfirmService.confirmPayment(paymentKey, orderId, amount));
	}
}