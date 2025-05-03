package com.jelly.zzirit.domain.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jelly.zzirit.domain.item.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
}