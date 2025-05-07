package com.jelly.zzirit.domain.item.entity.timedeal;

import java.time.LocalDateTime;

import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Column;
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
@AllArgsConstructor
public class TimeDeal extends BaseTime {

	@Column(name = "name", nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private TimeDealStatus status;

	@Column(name = "start_time", nullable = false)
	private LocalDateTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalDateTime endTime;

	@Column(name = "discount_ratio", nullable = false)
	private int discountRatio;

	public enum TimeDealStatus {
		SCHEDULED, ONGOING, ENDED
	}

	public void updateStatus(TimeDealStatus timeDealStatus) {
		this.status = timeDealStatus;
	}

}