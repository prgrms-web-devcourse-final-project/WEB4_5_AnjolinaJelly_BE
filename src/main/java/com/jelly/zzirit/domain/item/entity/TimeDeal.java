package com.jelly.zzirit.domain.item.entity;

import java.time.LocalDateTime;

import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeDeal extends BaseTime {

	private String name;

	@Enumerated(EnumType.STRING)
	private TimeDealStatus status;

	private LocalDateTime startTime;
	private LocalDateTime endTime;

	private int discountRatio;

	public enum TimeDealStatus {
		SCHEDULED, ONGOING, ENDED
	}
}