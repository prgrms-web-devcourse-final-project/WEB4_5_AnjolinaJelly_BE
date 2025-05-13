package com.jelly.zzirit.domain.cart.mapper;

import com.jelly.zzirit.domain.cart.dto.response.CartItemFetchResponse;
import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

public class CartItemMapper {

	public static CartItemFetchResponse mapToCartItem(CartItem cartItem, ItemStock itemStock, TimeDealItem timeDealItem) {
		Item item = cartItem.getItem();
		int quantity = cartItem.getQuantity();
		int originalPrice = item.getPrice().intValue();

		boolean isTimeDeal = item.getItemStatus() == ItemStatus.TIME_DEAL;

		int discountedPrice = isTimeDeal
			? timeDealItem.getPrice().intValue()
			: originalPrice;

		Integer discountRatio = isTimeDeal
			? timeDealItem.getTimeDeal().getDiscountRatio()
			: null;

		int totalPrice = discountedPrice * quantity;
		boolean isSoldOut = itemStock.getQuantity() == 0;

		String typeName = item.getTypeBrand().getType().getName();
		String brandName = item.getTypeBrand().getBrand().getName();

		return new CartItemFetchResponse(
			cartItem.getId(),
			item.getId(),
			item.getName(),
			typeName,
			brandName,
			quantity,
			item.getImageUrl(),
			originalPrice,
			discountedPrice,
			totalPrice,
			isTimeDeal,
			discountRatio,
			isSoldOut
		);
	}
}
