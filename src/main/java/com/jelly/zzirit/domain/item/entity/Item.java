package com.jelly.zzirit.domain.item.entity;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.adminItem.dto.request.ItemCreateRequest;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Item extends BaseTime {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type_id", nullable = false)
	private Type type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "brand_id", nullable = false)
	private Brand brand;

	private String name;

	@Column(name = "image_url")
	private String imageUrl;

	private BigDecimal price;

	public Empty update (ItemCreateRequest request, Type type, Brand brand) {
		this.type = type;
		this.brand = brand;
		this.name = request.name();
		this.price = BigDecimal.valueOf(request.price()); // todo: bigdecimal로 변경 필요
		return Empty.getInstance();
	}
}