package com.jelly.zzirit.domain.item.entity.timedeal;

import java.time.LocalDateTime;

import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.*;
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
@Table(name = "time_deal", indexes = {
	@Index(name = "idx_status_start_time", columnList = "discount_ratio, start_time")
})
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

	public static TimeDeal from(TimeDealCreateRequest request) {
		return new TimeDeal(
			request.title(),
			TimeDealStatus.SCHEDULED,
			request.startTime(),
			request.endTime(),
			request.discountRatio()
		);
	}

	public void updateStatus(TimeDealStatus timeDealStatus) {
		this.status = timeDealStatus;
	}

}