package com.jelly.zzirit.domain.order.entity;

import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTime {

	@Column(name = "payment_key", nullable = false, unique = true)
	private String paymentKey;

	@Column(name = "payment_method", nullable = false)
	@Enumerated(EnumType.STRING)
	private PaymentMethod paymentMethod;

	@Column(name = "payment_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;

	public enum PaymentStatus {
		READY, DONE, CANCELLED, FAILED
	}

	public static Payment of(String paymentKey, String methodRaw) {
		PaymentMethod method = PaymentMethod.from(methodRaw);
		return Payment.builder()
			.paymentKey(paymentKey)
			.paymentMethod(method)
			.paymentStatus(PaymentStatus.DONE)
			.build();
	}

	public void markCancelled() {
		paymentStatus = PaymentStatus.CANCELLED;
	}

	public void markFailed() {
		paymentStatus = PaymentStatus.FAILED;
	}

	public void changeStatus(PaymentStatus newStatus) {
		this.paymentStatus = newStatus;
	}
}