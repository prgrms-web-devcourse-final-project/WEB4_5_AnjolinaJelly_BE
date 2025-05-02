package com.jelly.zzirit.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.order.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}