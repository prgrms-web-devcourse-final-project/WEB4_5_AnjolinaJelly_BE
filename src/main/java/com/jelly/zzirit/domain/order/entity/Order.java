package com.jelly.zzirit.domain.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseTime {

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	private BigDecimal totalPrice;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	private String shippingRequest;

	@Column(name = "order_number")
	private UUID orderNumber;

	public enum OrderStatus {
		PENDING, PAID, FAILED, CANCELLED, COMPLETED
	}
}