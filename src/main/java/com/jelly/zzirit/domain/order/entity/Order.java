package com.jelly.zzirit.domain.order.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequestDto;
import com.jelly.zzirit.global.entity.BaseTime;

import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.EXPIRED_CANCEL_TIME;
import static com.jelly.zzirit.global.dto.BaseResponseStatus.NOT_PAID_ORDER;

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

	public static Order tempOf(Member member, String orderNumber, PaymentRequestDto dto) {
		return Order.builder()
			.member(member)
			.orderNumber(orderNumber)
			.totalPrice(dto.totalAmount())
			.status(OrderStatus.PENDING)
			.shippingRequest(dto.shippingRequest())
			.address(dto.address())
			.addressDetail(dto.addressDetail())
			.build();
	}

	public void changeStatus(OrderStatus newStatus) {
		this.status = newStatus;
	}

	public void addOrderItem(OrderItem orderItem) {
		orderItems.add(orderItem);
	}

	public boolean isOwnedBy(Long memberId) {
		return this.getMember().getId().equals(memberId);
	}

	public void checkCancellation() {
		if (status != OrderStatus.PAID) { // 결제 완료 상태인 주문만 취소 가능
			throw new InvalidOrderException(NOT_PAID_ORDER);
		}

		if (this.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) { // 24시간 이내의 주문만 취소 가능
			throw new InvalidOrderException(EXPIRED_CANCEL_TIME);
		}
	}

	public void cancel() {
		this.status = OrderStatus.CANCELLED;
	}

	public boolean isConfirmed() {
		return this.status == OrderStatus.PAID || this.status == OrderStatus.COMPLETED;
	}
}