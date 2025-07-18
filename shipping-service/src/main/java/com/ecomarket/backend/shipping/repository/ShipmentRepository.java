package com.ecomarket.backend.shipping.repository;

import com.ecomarket.backend.shipping.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByOrderId(Integer orderId);
    List<Shipment> findBySupplierSupplierId(Integer supplierId);
    List<Shipment> findByShipmentStatus(Shipment.ShipmentStatus status);
}
