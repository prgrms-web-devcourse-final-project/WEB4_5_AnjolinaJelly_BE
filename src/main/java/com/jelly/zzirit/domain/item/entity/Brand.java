package com.jelly.zzirit.domain.item.entity;

import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends BaseTime {

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	public Brand(String name) {
		this.name = name;
	}
}