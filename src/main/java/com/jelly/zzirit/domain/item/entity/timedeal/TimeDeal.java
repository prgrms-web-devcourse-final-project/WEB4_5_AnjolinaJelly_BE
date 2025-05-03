package com.jelly.zzirit.domain.item.entity.timedeal;

import java.time.LocalDateTime;

import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	public boolean isActiveNow() {
		LocalDateTime now = LocalDateTime.now();
		return now.isAfter(startTime) && now.isBefore(endTime);
	}
}