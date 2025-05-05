package com.jelly.zzirit.domain.order.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.*;
import lombok.*;
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

	@Column(precision = 10, scale = 2, nullable = false)
	private BigDecimal totalPrice;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private OrderStatus status;

	@Column(name = "shipping_request")
	private String shippingRequest;

	@Column(name = "order_number", unique = true, nullable = false, length = 30)
	private String orderNumber;

	@Builder.Default
	@OneToMany(mappedBy = "order", orphanRemoval = true, cascade = CascadeType.ALL)
	private List<OrderItem> orderItems = new ArrayList<>();

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "address_detail")
	private String addressDetail;

	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "payment_id")
	private Payment payment;

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

	public void addOrderItem(OrderItem orderItem) {
		orderItems.add(orderItem);
	}

}