package com.jelly.zzirit.domain.order.repository.order;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.jelly.zzirit.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.order.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    @Query("SELECT o.id FROM Order o " +
           "WHERE o.member.id = :memberId " +
           "AND o.status IN :statuses")
    Page<Long> findOrderIdsByMemberIdAndStatuses(
        @Param("memberId") Long memberId,
        @Param("statuses") Collection<OrderStatus> statuses,
        Pageable pageable
    );

    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.orderItems oi " +
           "JOIN FETCH oi.item " +
           "WHERE o.id IN :ids")
    List<Order> findByIdsWithItems(@Param("ids") List<Long> ids);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.payment " +
           "WHERE o.id = :orderId")
    Optional<Order> findByIdWithPayment(@Param("orderId") Long orderId);

    List<Order> findAllByStatusAndCreatedAtBefore(OrderStatus orderStatus, LocalDateTime deadline);

}