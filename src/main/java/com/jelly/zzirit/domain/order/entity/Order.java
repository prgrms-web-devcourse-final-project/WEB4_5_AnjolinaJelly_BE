package com.jelly.zzirit.domain.order.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseTime {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	private BigDecimal totalPrice;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	private String shippingRequest;

	@Column(name = "order_number", unique = true, nullable = false, length = 30)
	private String orderNumber;

	public enum OrderStatus {
		PENDING, PAID, FAILED, CANCELLED, COMPLETED
	}

	public static String generateOrderNumber(long sequence) {
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		return String.format("ORD%s-%06d", date, sequence);
	}

	public static Order of(Member member, String orderNumber, BigDecimal totalPrice, String shippingRequest) {
		return Order.builder()
			.member(member)
			.orderNumber(orderNumber)
			.totalPrice(totalPrice)
			.status(OrderStatus.PAID)
			.shippingRequest(shippingRequest)
			.build();
	}
}