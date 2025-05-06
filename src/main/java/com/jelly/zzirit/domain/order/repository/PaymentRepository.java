package com.jelly.zzirit.domain.order.repository;

import java.util.Optional;

import com.jelly.zzirit.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.order.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByPaymentKey(String paymentKey);

	Optional<Payment> findByOrder(Order order);

}