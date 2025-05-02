package com.jelly.zzirit.domain.item.entity.stock;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.global.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
public class TimeDealStock extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "time_deal_item_id", nullable = false, unique = true)
	private TimeDealItem timeDealItem;

	private int quantity; // 타임딜 최초 설정된 총 재고

	private int reservedQuantity; // 결제 진행 중인 미확정 수량

	private int soldQuantity; // 결제 완료되어 확정된 수량
}

// 단일 quantity 는 "누가 선점했는지", "확정인지", "대기 중인지" 구분이 안 돼서 정합성 흐트러짐