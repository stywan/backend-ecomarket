package com.ecomarket.backend.shipping.repository;

import com.ecomarket.backend.shipping.model.ShipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentStatusHistoryRepository extends JpaRepository<ShipmentStatusHistory, Integer> {
    List<ShipmentStatusHistory> findByShipmentShipmentIdOrderByEventDatetimeAsc(Integer shipmentId);
}

