package com.jelly.zzirit.domain.item.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.item.entity.stock.ItemStock;

@Repository
public interface ItemStockRepository extends JpaRepository<ItemStock, Long> {
	// 상품 id 목록을 받아서 재고개수 목록 조회
	List<ItemStock> findAllByItemIdIn(List<Long> itemIds);
	Optional<ItemStock> findByItemId(Long itemId);
}