package com.jelly.zzirit.domain.timeDeal.dto.response;

import java.util.List;

import com.jelly.zzirit.domain.timeDeal.dto.TimeDealCreateItem;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "타임딜 등록 응답")
public class TimeDealCreateResponse {
	@Schema(description = "타임딜 ID", example = "12345")
	public Long timeDealId;

	@Schema(description = "타임딜 제목", example = "노트북 90% 할인")
	public String title;

	@Schema(description = "시작 시간", example = "2025-05-01T00:00:00")
	public String startTime;

	@Schema(description = "종료 시간", example = "2025-05-01T12:00:00")
	public String endTime;

	@Schema(description = "할인율 (%)", example = "90")
	public Integer discountRate;

	@Schema(description = "타임딜 대상 상품 목록")
	public List<TimeDealCreateItem> items;

	public TimeDealCreateResponse(Long timeDealId, String title, String startTime, String endTime, Integer discountRate,
		List<TimeDealCreateItem> items) {
		this.timeDealId = timeDealId;
		this.title = title;
		this.startTime = startTime;
		this.endTime = endTime;
		this.discountRate = discountRate;
		this.items = items;
	}
}
