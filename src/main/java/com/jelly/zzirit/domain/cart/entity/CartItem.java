package com.jelly.zzirit.domain.cart.entity;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.entity.BaseTime;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseTime {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	@Setter
	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	public static CartItem of(Cart cart, Item item, Integer quantity) {
		return CartItem.builder()
			.cart(cart)
			.item(item)
			.quantity(quantity)
			.build();
	}

	public void changeQuantity(int newQuantity) {
		if (newQuantity < 1) {
			throw new InvalidItemException(BaseResponseStatus.INVALID_CART_QUANTITY);
		}
		this.quantity = newQuantity;
	}
}