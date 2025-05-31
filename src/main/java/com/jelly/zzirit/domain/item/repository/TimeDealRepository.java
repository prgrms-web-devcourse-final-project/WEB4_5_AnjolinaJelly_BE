package com.jelly.zzirit.domain.item.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

import io.lettuce.core.dynamic.annotation.Param;

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

	@Query("SELECT t FROM TimeDeal t WHERE DATE(t.startTime) = :today OR DATE(t.endTime) = :today")
	List<TimeDeal> findByStartOrEndDate(@Param("today") LocalDate today);

	@Query("""
		    SELECT t FROM TimeDeal t
		    WHERE (t.startTime BETWEEN :now AND :endOfDay) 
		       OR (t.endTime BETWEEN :now AND :endOfDay)
		""")
	List<TimeDeal> findByStartOrEndBetween(@Param("now") LocalDateTime now, @Param("endOfDay") LocalDateTime endOfDay);

	@Query("SELECT t FROM TimeDeal t WHERE DATE(t.startTime) = :tomorrow OR DATE(t.endTime) = :tomorrow")
	List<TimeDeal> findByStartOrEndDateTomorrow(@Param("tomorrow") LocalDate tomorrow);

}
