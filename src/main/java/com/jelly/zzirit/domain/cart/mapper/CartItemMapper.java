package com.jelly.zzirit.domain.cart.mapper;

import com.jelly.zzirit.domain.cart.dto.response.CartItemFetchResponse;
import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

public class CartItemMapper {

	public static CartItemFetchResponse mapToCartItem(CartItem cartItem, ItemStock itemStock,
		TimeDealItem timeDealItem) {
		return mapToCartItem(cartItem, itemStock, timeDealItem, cartItem.getQuantity());
	}

	public static CartItemFetchResponse mapToCartItem(CartItem cartItem, ItemStock itemStock,
		TimeDealItem timeDealItem, int quantity) {
		Item item = cartItem.getItem();

		int originalPrice = item.getPrice().intValue();
		int discountedPrice = originalPrice;
		Integer discountRatio = null;
		boolean isTimeDeal = false;

		if (timeDealItem != null) {
			discountedPrice = timeDealItem.getPrice().intValue();
			discountRatio = timeDealItem.getTimeDeal().getDiscountRatio();
			isTimeDeal = true;
		}

		int totalPrice = discountedPrice * quantity;
		boolean isSoldOut = itemStock.getQuantity() == 0;

		return new CartItemFetchResponse(
			cartItem.getId(),
			item.getId(),
			item.getName(),
			item.getTypeBrand().getType().getName(),
			item.getTypeBrand().getBrand().getName(),
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