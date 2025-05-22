package com.jelly.zzirit.domain.item.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import org.springframework.transaction.annotation.Transactional;

public interface TimeDealRepository extends JpaRepository<TimeDeal, Long> {

	@Query("SELECT t FROM TimeDeal t where t.status = 'ONGOING'")
	List<TimeDeal> getOngoingTimeDeal();

	List<TimeDeal> findByNameContaining(String timeDealName);

	TimeDeal findByStatusAndEndTimeBefore(TimeDeal.TimeDealStatus status, LocalDateTime endTime);

	TimeDeal findByStatusAndStartTimeLessThanEqual(TimeDeal.TimeDealStatus status, LocalDateTime now);

	TimeDeal findTopByStatusOrderByStartTimeAsc(TimeDeal.TimeDealStatus status);

	// ✅ 가장 늦은 타임딜 종료 시간 조회
	@Query("SELECT MAX(t.endTime) FROM TimeDeal t")
	Optional<LocalDateTime> findMaxEndTime();

	// ✅ ID 기준 내림차순으로 n개 삭제 (native 쿼리)
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM time_deal WHERE id IN (SELECT id FROM time_deal ORDER BY id DESC LIMIT :limit)", nativeQuery = true)
	void deleteTopNByIdDesc(@Param("limit") int limit);
}
