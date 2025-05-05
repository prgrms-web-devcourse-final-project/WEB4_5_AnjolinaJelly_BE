package com.jelly.zzirit.domain.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

@Repository
public interface TimeDealStockRepository extends JpaRepository<TimeDealStock, Long> {

	@Modifying
	@Query("""
		    UPDATE TimeDealStock s
		    SET s.reservedQuantity = s.reservedQuantity - :qty,
		        s.soldQuantity = s.soldQuantity + :qty
		    WHERE s.timeDealItem.id = :timeDealItemId
		      AND s.reservedQuantity >= :qty
		""")
	int confirmStock(@Param("timeDealItemId") Long timeDealItemId, @Param("qty") int qty);

	@Query("""
		SELECT s.timeDealItem
		FROM TimeDealStock s
		WHERE s.timeDealItem.id = :timeDealItemId
		""")
	Optional<TimeDealItem> findTimeDealItemById(@Param("timeDealItemId") Long timeDealItemId);
}