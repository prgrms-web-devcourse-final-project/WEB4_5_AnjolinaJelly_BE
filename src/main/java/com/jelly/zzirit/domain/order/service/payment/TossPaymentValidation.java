package com.jelly.zzirit.domain.order.service.payment;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderStatus;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TossPaymentValidation {

	PAYMENT_AMOUNT_MISMATCH {
		@Override
		public void validate(Order order, PaymentResponse response, String amount) {
			if (order.getTotalPrice().compareTo(new BigDecimal(amount)) != 0) {
				throw new InvalidOrderException(BaseResponseStatus.PAYMENT_AMOUNT_MISMATCH);
			}
		}
	},

	ORDER_ID_MISMATCH {
		@Override
		public void validate(Order order, PaymentResponse response, String amount) {
			if (!order.getOrderNumber().equals(response.getOrderId())) {
				throw new InvalidOrderException(BaseResponseStatus.ORDER_ID_MISMATCH);
			}
		}
	},

	ALREADY_PROCESSED {
		@Override
		public void validate(Order order, PaymentResponse response, String amount) {
			if (order.getStatus() != OrderStatus.PENDING) {
				throw new InvalidOrderException(BaseResponseStatus.ALREADY_PROCESSED);
			}
		}
	},

	TOSS_AMOUNT_MISMATCH {
		@Override
		public void validate(Order order, PaymentResponse response, String amount) {
			if (response.getTotalAmount().compareTo(new BigDecimal(amount)) != 0) {
				throw new InvalidOrderException(BaseResponseStatus.TOSS_PAYMENT_AMOUNT_MISMATCH);
			}
		}
	};

	public abstract void validate(Order order, PaymentResponse response, String amount);

	public static void validateAll(Order order, PaymentResponse response, String amount) {
		for (TossPaymentValidation status : values()) {
			status.validate(order, response, amount);
		}
	}
}