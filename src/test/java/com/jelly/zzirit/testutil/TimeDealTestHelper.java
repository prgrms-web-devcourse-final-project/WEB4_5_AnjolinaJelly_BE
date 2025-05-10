package com.jelly.zzirit.testutil;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.item.dto.response.timeDeal.TimeDealCreateResponse;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.service.TimeDealService;

import jakarta.persistence.EntityManager;

@Component
public class TimeDealTestHelper {

	private final TimeDealService timeDealService;
	private final EntityManager em;

	public TimeDealTestHelper(TimeDealService timeDealService, EntityManager em) {
		this.timeDealService = timeDealService;
		this.em = em;
	}

	@Transactional
	public TimeDealCreateResponse createOngoingTimeDeal(TimeDealCreateRequest request) {
		TimeDealCreateResponse response = timeDealService.createTimeDeal(request);
		TimeDeal timeDeal = em.find(TimeDeal.class, response.timeDealId());
		timeDeal.updateStatus(TimeDeal.TimeDealStatus.ONGOING);
		return response;
	}
}