package com.jelly.zzirit.domain.item.entity.stock;

import com.jelly.zzirit.domain.adminItem.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.entity.BaseEntity;

import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemStock extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false, unique = true)
	private Item item;

	private int quantity; // 최초 설정된 총 재고

	private int reservedQuantity; // 결제 진행 중인 미확정 수량

	private int soldQuantity; // 결제 완료되어 확정된 수량

	public Empty update (ItemCreateRequest request, Item item) {
		this.item = item;

		// 팔린 만큼 재고 채워넣어야 함(임시 비즈니스 로직)
		if (request.stockQuantity() < this.soldQuantity + this.reservedQuantity) {
			throw new InvalidItemException(BaseResponseStatus.OUT_OF_STOCK);
		}
		
		this.quantity = request.stockQuantity(); // todo: 최초 설정된 재고만 업데이트 가능?
		return Empty.getInstance();
	}
}

// 단일 quantity 는 "누가 선점했는지", "확정인지", "대기 중인지" 구분이 안 돼서 정합성 흐트러짐