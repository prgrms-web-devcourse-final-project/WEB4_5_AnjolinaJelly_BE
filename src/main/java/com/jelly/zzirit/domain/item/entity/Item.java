package com.jelly.zzirit.domain.item.entity;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.admin.dto.request.ItemCreateRequest;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.entity.BaseTime;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@AllArgsConstructor
public class Item extends BaseTime {

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Setter
	@Column(name = "image_url", nullable = false)
	private String imageUrl;

	@Column(name = "price", nullable = false)
	private BigDecimal price;

	@Column(name = "item_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ItemStatus itemStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type_brand_id", nullable = false)
	private TypeBrand typeBrand;

	// update함수는 entity 수정 pr 머지 이후에 수정하는 게 좋을 것 같아요!
	public Empty update(ItemCreateRequest request, TypeBrand typeBrand) {
		this.name = request.name();
		this.price = request.price(); // todo: bigdecimal로 변경 필요
		this.typeBrand = typeBrand;

		return Empty.getInstance();
	}

	public Empty changePrice(BigDecimal newPrice) {
		if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
			throw new InvalidItemException(BaseResponseStatus.INVALID_PRICE);
		}
		this.price = newPrice;

		return Empty.getInstance();
	}

	// todo: itemStatus 업데이트 로직 추가해야 함
	// 임시 테스트 용
	public Empty changeItemStatus(ItemStatus status) {
		this.itemStatus = status;
		return Empty.getInstance();
	}

	public boolean validateTimeDeal() {
		return this.getItemStatus().equals(ItemStatus.TIME_DEAL);
	}

	public void updatePriceAndImageUrl(BigDecimal price, String imageUrl) {
		this.price = price;
		this.imageUrl = imageUrl;
	}

}