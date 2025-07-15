package com.ecomarket.backend.cart_order.repository;


import com.ecomarket.backend.cart_order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

}
