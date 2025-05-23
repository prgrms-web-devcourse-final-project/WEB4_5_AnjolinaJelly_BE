package com.jelly.zzirit.domain.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

public interface ItemRepository extends JpaRepository<Item, Long> {

    default Item getById(Long itemId) {
        return findById(itemId).orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));
    }
}