package com.jelly.zzirit.domain.item.repository;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemResponse;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // 1. 이름으로 검색
    @Query("""
        SELECT new com.jelly.zzirit.domain.admin.dto.response.AdminItemResponse(
            i.id, i.name, i.imageUrl, t.name, b.name, i.price,
            s.quantity, s.soldQuantity
        )
        FROM Item i
        JOIN i.typeBrand tb
        JOIN tb.type t
        JOIN tb.brand b
        JOIN ItemStock s ON s.item = i
        WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    Page<AdminItemResponse> searchItemsByName(@Param("name") String name, Pageable pageable);

    // 2. ID로 단건 조회 (있으면 1건, 없으면 빈 Page)
    @Query("""
        SELECT new com.jelly.zzirit.domain.admin.dto.response.AdminItemResponse(
            i.id, i.name, i.imageUrl, t.name, b.name, i.price,
            s.quantity, s.soldQuantity
        )
        FROM Item i
        JOIN i.typeBrand tb
        JOIN tb.type t
        JOIN tb.brand b
        JOIN ItemStock s ON s.item = i
        WHERE i.id = :itemId
    """)
    Page<AdminItemResponse> searchItemById(@Param("itemId") Long itemId, Pageable pageable);

    // 3. 전체 조회
    @Query("""
        SELECT new com.jelly.zzirit.domain.admin.dto.response.AdminItemResponse(
            i.id, i.name, i.imageUrl, t.name, b.name, i.price,
            s.quantity, s.soldQuantity
        )
        FROM Item i
        JOIN i.typeBrand tb
        JOIN tb.type t
        JOIN tb.brand b
        JOIN ItemStock s ON s.item = i
    """)
    Page<AdminItemResponse> findAllItems(Pageable pageable);

    Page<Item> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    default Item getById(Long itemId) {
        return findById(itemId).orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));
    }
}