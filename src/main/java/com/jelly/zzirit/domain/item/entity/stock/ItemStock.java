package com.jelly.zzirit.domain.item.entity.stock;

import com.jelly.zzirit.domain.adminItem.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.entity.BaseEntity;

import com.jelly.zzirit.global.exception.custom.InvalidItemException;

import jakarta.persistence.Column;
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

	// 단일 quantity 는 "누가 선점했는지", "확정인지", 구분이 안 돼서 정합성 흐트러짐
	@Column(name = "quantity", nullable = false)
	private int quantity; // 최초 설정된 총 재고

	@Column(name = "sold_quantity")
	private int soldQuantity; // 결제 완료되어 확정된 수량

	public Empty update (ItemCreateRequest request, Item item) {
		this.item = item;

		// 팔린 만큼 재고 채워넣어야 함(임시 비즈니스 로직)
		if (request.stockQuantity() < this.soldQuantity) {
			throw new InvalidItemException(BaseResponseStatus.OUT_OF_STOCK);
		}

		this.quantity = request.stockQuantity(); // todo: 최초 설정된 재고만 업데이트 가능?
		return Empty.getInstance();
	}

	public void addSoldQuantity(int quantity) {
		this.soldQuantity += quantity;
	}
}