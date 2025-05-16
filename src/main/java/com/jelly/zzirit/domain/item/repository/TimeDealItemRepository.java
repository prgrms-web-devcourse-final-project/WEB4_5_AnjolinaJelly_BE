package com.jelly.zzirit.domain.item.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

@Repository
public interface TimeDealItemRepository extends JpaRepository<TimeDealItem, Long> {

	@Query("SELECT tdi FROM TimeDealItem tdi WHERE tdi.item.id = :itemId AND tdi.timeDeal.startTime <= CURRENT_TIMESTAMP AND tdi.timeDeal.endTime >= CURRENT_TIMESTAMP")
	Optional<TimeDealItem> findActiveTimeDealItemByItemId(@Param("itemId") Long itemId);

	List<TimeDealItem> findAllByTimeDeal(TimeDeal timeDeal);

	List<TimeDealItem> findByItem_NameContaining(String timeDealItemName);

	TimeDealItem findTimeDealItemById(Long timeDealItemId);
}