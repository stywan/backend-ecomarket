package com.ecomarket.backend.cart_order.repository;

import com.ecomarket.backend.cart_order.model.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
    List<Return> findByOrderId(Long orderId);
}
