package com.jelly.zzirit.domain.item.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TimeDealRepository extends JpaRepository<TimeDeal, Long> {

	@Query("SELECT t FROM TimeDeal t where t.status = 'ONGOING'")
	List<TimeDeal> getOngoingTimeDeal();

	List<TimeDeal> findByNameContaining(String timeDealName);

	List<TimeDeal> findByStatusAndEndTimeBefore(TimeDeal.TimeDealStatus status, LocalDateTime endTime);

	List<TimeDeal> findByStatusAndStartTimeLessThanEqual(TimeDeal.TimeDealStatus status, LocalDateTime now);

	TimeDeal findTopByStatusOrderByStartTimeAsc(TimeDeal.TimeDealStatus status);

	// 가장 늦은 타임딜 종료 시간 조회
	@Query("SELECT MAX(t.endTime) FROM TimeDeal t")
	Optional<LocalDateTime> findMaxEndTime();

	@Query(value = "SELECT id FROM time_deal ORDER BY id DESC LIMIT :limit", nativeQuery = true)
	List<Long> findTopNIdsByIdDesc(@Param("limit") int limit);

	@Modifying
	@Transactional
	@Query("DELETE FROM TimeDeal t WHERE t.id IN :ids")
	void deleteByIds(@Param("ids") List<Long> ids);

	@Query("SELECT t FROM TimeDeal t " +
		   "WHERE t.status IN :statuses " +
		   "ORDER BY t.startTime")
	List<TimeDeal> findInitTimeDeals(@Param("statuses") List<TimeDeal.TimeDealStatus> statuses);

}
