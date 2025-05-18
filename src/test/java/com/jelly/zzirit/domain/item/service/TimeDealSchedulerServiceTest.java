package com.jelly.zzirit.domain.item.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.global.support.AcceptanceTest;

@DisplayName("TimeDealSchedulerService 통합 테스트")
class TimeDealSchedulerServiceTest extends AcceptanceTest {

	@Autowired
	private TimeDealSchedulerService timeDealSchedulerService;

	@Autowired
	private TimeDealRepository timeDealRepository;

	@Test
	void SCHEDULED_상태의_타임딜을_ONGOING으로_변경한다() {
		// given
		TimeDeal deal = timeDealRepository.save(TimeDeal.builder()
			.name("예정된 타임딜")
			.startTime(LocalDateTime.now().minusMinutes(1))
			.endTime(LocalDateTime.now().plusDays(1))
			.status(TimeDealStatus.SCHEDULED)
			.discountRatio(10)
			.build());

		// when
		int updatedCount = timeDealSchedulerService.startScheduledDeals(LocalDateTime.now());

		// then
		TimeDeal updatedDeal = timeDealRepository.findById(deal.getId()).orElseThrow();
		assertThat(updatedCount).isEqualTo(1);
		assertThat(updatedDeal.getStatus()).isEqualTo(TimeDealStatus.ONGOING);
	}

	@Test
	void ONGOING_상태의_타임딜을_ENDED로_변경한다() {
		// given
		TimeDeal deal = timeDealRepository.save(TimeDeal.builder()
			.name("진행 중 타임딜")
			.startTime(LocalDateTime.now().minusDays(2))
			.endTime(LocalDateTime.now().minusMinutes(1))
			.status(TimeDealStatus.ONGOING)
			.discountRatio(20)
			.build());

		// when
		int updatedCount = timeDealSchedulerService.endOngoingDeals(LocalDateTime.now());

		// then
		TimeDeal updatedDeal = timeDealRepository.findById(deal.getId()).orElseThrow();
		assertThat(updatedCount).isEqualTo(1);
		assertThat(updatedDeal.getStatus()).isEqualTo(TimeDealStatus.ENDED);
	}
}