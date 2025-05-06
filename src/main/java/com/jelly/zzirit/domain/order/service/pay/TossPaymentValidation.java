package com.jelly.zzirit.domain.order.service.pay;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.order.dto.response.TossPaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TossPaymentValidation {

	PAYMENT_AMOUNT_MISMATCH {
		@Override
		public void validate(Order order, TossPaymentResponse response, String amount) {
			if (order.getTotalPrice().compareTo(new BigDecimal(amount)) != 0) {
				throw new InvalidOrderException(BaseResponseStatus.PAYMENT_AMOUNT_MISMATCH);
			}
		}
	},

	ORDER_ID_MISMATCH {
		@Override
		public void validate(Order order, TossPaymentResponse response, String amount) {
			if (!order.getOrderNumber().equals(response.getOrderId())) {
				throw new InvalidOrderException(BaseResponseStatus.ORDER_ID_MISMATCH);
			}
		}
	},

	ALREADY_PROCESSED {
		@Override
		public void validate(Order order, TossPaymentResponse response, String amount) {
			if (order.getStatus() != Order.OrderStatus.PENDING) {
				throw new InvalidOrderException(BaseResponseStatus.ALREADY_PROCESSED);
			}
		}
	},

	PAYMENT_STATUS_INVALID {
		@Override
		public void validate(Order order, TossPaymentResponse response, String amount) {
			if (!"DONE".equalsIgnoreCase(response.getStatus())) {
				throw new InvalidOrderException(BaseResponseStatus.TOSS_PAYMENT_NOT_DONE);
			}
		}
	},

	TOSS_AMOUNT_MISMATCH {
		@Override
		public void validate(Order order, TossPaymentResponse response, String amount) {
			if (response.getTotalAmount().compareTo(new BigDecimal(amount)) != 0) {
				throw new InvalidOrderException(BaseResponseStatus.TOSS_PAYMENT_AMOUNT_MISMATCH);
			}
		}
	};

	public abstract void validate(Order order, TossPaymentResponse response, String amount);

	public static void validateAll(Order order, TossPaymentResponse response, String amount) {
		for (TossPaymentValidation status : values()) {
			status.validate(order, response, amount);
		}
	}
}