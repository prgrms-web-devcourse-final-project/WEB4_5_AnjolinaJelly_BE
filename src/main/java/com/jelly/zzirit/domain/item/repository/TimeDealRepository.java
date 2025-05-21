package com.jelly.zzirit.domain.item.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

public interface TimeDealRepository extends JpaRepository<TimeDeal, Long> {

	@Query("SELECT t FROM TimeDeal t where t.status = 'ONGOING'")
	List<TimeDeal> getOngoingTimeDeal();

	List<TimeDeal> findByNameContaining(String timeDealName);

	TimeDeal findByStatusAndEndTimeBefore(TimeDeal.TimeDealStatus status, LocalDateTime endTime);

	TimeDeal findByStatusAndStartTimeLessThanEqual(TimeDeal.TimeDealStatus status, LocalDateTime now);
}
