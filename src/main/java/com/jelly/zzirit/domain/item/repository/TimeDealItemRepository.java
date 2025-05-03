package com.jelly.zzirit.domain.item.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

@Repository
public interface TimeDealItemRepository extends JpaRepository<TimeDealItem, Long> {

	@Query("""
			select tdi
			from TimeDealItem tdi
			join fetch tdi.timeDeal td
			where tdi.id = :id
		""")
	Optional<TimeDealItem> findWithDeal(@Param("id") Long timeDealItemId);
}