package com.jelly.zzirit.domain.order.entity;

import java.math.BigDecimal;

import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
public class Payment extends BaseTime {

	private BigDecimal amount;

	private String impUid;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	@OneToOne
	@JoinColumn(name = "order_id")
	private Order order;
}