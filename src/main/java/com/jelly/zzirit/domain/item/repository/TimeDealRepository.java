package com.jelly.zzirit.domain.item.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

public interface TimeDealRepository extends JpaRepository<TimeDeal, Long> {

	@Query("SELECT t FROM TimeDeal t where t.status = 'ONGOING'")
	Optional<TimeDeal> getOngoingTimeDeal();

	List<TimeDeal> findByNameContaining(String timeDealName);

}
