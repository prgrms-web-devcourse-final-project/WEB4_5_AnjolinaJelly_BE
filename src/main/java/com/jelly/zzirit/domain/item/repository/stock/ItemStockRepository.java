package com.jelly.zzirit.domain.item.repository.stock;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemStockRepository extends JpaRepository<ItemStock, Long>, ItemStockRepositoryCustom {
	// 상품 id 목록을 받아서 재고개수 목록 조회
	List<ItemStock> findAllByItemIdIn(List<Long> itemIds);

	Optional<ItemStock> findByItemId(Long itemId);

	List<ItemStock> findAllByItemId(Long itemId);

	Optional<ItemStock> findByTimeDealItem(TimeDealItem tdi);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM ItemStock s WHERE s.item = :item")
	Optional<ItemStock> findByItemWithPessimisticLock(@Param("item") Item item);

	@Query("SELECT s FROM ItemStock s WHERE s.timeDealItem.id = :timeDealItemId")
	Optional<ItemStock> findByTimeDealItemId(@Param("timeDealItemId") Long timeDealItemId);
}