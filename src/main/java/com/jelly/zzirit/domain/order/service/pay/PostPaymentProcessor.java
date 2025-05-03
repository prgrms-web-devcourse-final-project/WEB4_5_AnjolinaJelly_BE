package com.jelly.zzirit.domain.order.service.pay;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.order.dto.request.RedisOrderData;
import com.jelly.zzirit.domain.order.service.OrderCreator;
import com.jelly.zzirit.domain.order.service.StockConfirmer;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostPaymentProcessor {

	private final OrderCreator orderCreator;
	private final StockConfirmer stockConfirmer;

	public void process(String orderId, RedisOrderData cached, BigDecimal paidAmount) {

		if (cached.getTotalAmount().compareTo(paidAmount) != 0) {
			throw new InvalidOrderException(BaseResponseStatus.PRICE_MANIPULATION_DETECTED);
		}

		orderCreator.createOrderWithItems(orderId, cached);

		for (RedisOrderData.ItemData item : cached.getItems()) {
			int updated = item.isTimeDeal()
				? stockConfirmer.confirmTimeDealStock(item.getTargetStockId(), item.getQuantity())
				: stockConfirmer.confirmItemStock(item.getTargetStockId(), item.getQuantity());

			if (updated == 0) {
				throw new InvalidOrderException(BaseResponseStatus.STOCK_CONFIRMATION_FAILED);
			}
		}
	}
}