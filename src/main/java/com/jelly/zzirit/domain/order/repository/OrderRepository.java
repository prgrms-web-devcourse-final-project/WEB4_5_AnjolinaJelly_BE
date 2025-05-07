package com.jelly.zzirit.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.order.entity.Order;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.orderItems oi " +
           "JOIN FETCH oi.item i " +
           "WHERE o.member.id = :memberId " +
           "AND o.status IN :statuses " +
           "ORDER BY o.createdAt DESC")
    List<Order> findAllByMemberIdWithItems(
        @Param("memberId") Long memberId,
        @Param("statuses") Collection<Order.OrderStatus> orderStatuses
    );

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.payment " +
           "WHERE o.id = :orderId")
    Optional<Order> findByIdWithPayment(@Param("orderId") Long orderId);

    List<Order> findAllByStatusAndCreatedAtBefore(Order.OrderStatus orderStatus, LocalDateTime deadline);

}