package com.jelly.zzirit.domain.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;

@Repository
public interface ItemStockRepository extends JpaRepository<ItemStock, Long> {

	@Modifying
	@Query("""
		    UPDATE ItemStock s
		    SET s.reservedQuantity = s.reservedQuantity - :qty,
		        s.soldQuantity = s.soldQuantity + :qty
		    WHERE s.item.id = :itemId
		      AND s.reservedQuantity >= :qty
		""")
	int confirmStock(@Param("itemId") Long itemId, @Param("qty") int qty);

	@Query("""
		    SELECT s.item
		    FROM ItemStock s
		    WHERE s.item.id = :itemId
		""")
	Optional<Item> findItemById(@Param("itemId") Long itemId);
}