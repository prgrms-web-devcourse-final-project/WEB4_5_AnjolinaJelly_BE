package com.jelly.zzirit.domain.item.entity;

import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Type extends BaseTime {

	@Column(nullable = false, length = 100)
	private String name;

	public Type(String name) {
		this.name = name;
	}
}