package com.ecomarket.backend.shipping.service;

import com.ecomarket.backend.shipping.DTO.request.ShipmentRequestDTO;
import com.ecomarket.backend.shipping.DTO.response.*;
import com.ecomarket.backend.shipping.client.OrderServiceClient;
import com.ecomarket.backend.shipping.model.Shipment;
import com.ecomarket.backend.shipping.model.ShipmentStatusHistory;
import com.ecomarket.backend.shipping.model.Supplier;
import com.ecomarket.backend.shipping.repository.ShipmentRepository;
import com.ecomarket.backend.shipping.repository.ShipmentStatusHistoryRepository;
import com.ecomarket.backend.shipping.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@AllArgsConstructor
@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final SupplierRepository supplierRepository;
    private final OrderServiceClient orderServiceClient;

    private final ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;

    private static final BigDecimal DEFAULT_SHIPPING_COST = BigDecimal.valueOf(3990.0);
    private static final int ESTIMATED_DELIVERY_DAYS = 3;


    @Transactional
    public Shipment createShipment(ShipmentRequestDTO shipmentRequestDTO) {
        Supplier supplier = supplierRepository.findById(shipmentRequestDTO.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + shipmentRequestDTO.getSupplierId()));

        OrderResponseDTO orderResponse = orderServiceClient.getOrderById(shipmentRequestDTO.getOrderId());
        if (orderResponse == null) {
            throw new IllegalArgumentException("Order not found with ID: " + shipmentRequestDTO.getOrderId());
        }

        Shipment shipment = new Shipment();
        shipment.setOrderId(Math.toIntExact(shipmentRequestDTO.getOrderId()));
        shipment.setTrackingNumber(shipmentRequestDTO.getTrackingNumber());
        shipment.setSupplier(supplier);

        LocalDateTime currentDateTime = LocalDateTime.now();
        shipment.setShipmentDate(currentDateTime);
        shipment.setEstimatedDeliveryDate(currentDateTime.plusDays(ESTIMATED_DELIVERY_DAYS));
        shipment.setShipmentStatus(Shipment.ShipmentStatus.PENDING);
        shipment.setShippingCost(DEFAULT_SHIPPING_COST);

        if (orderResponse.getShippingAddressId() == null) {
            throw new IllegalArgumentException("Shipping Address ID is missing in the Order with ID: " + shipmentRequestDTO.getOrderId());
        }
        shipment.setDestinationAddressId(Math.toIntExact(orderResponse.getShippingAddressId()));

        Shipment savedShipment = shipmentRepository.save(shipment);

        createShipmentStatusHistoryEntry(savedShipment, savedShipment.getShipmentStatus().name(), "Initial shipment status.");

        return savedShipment;
    }

    @Transactional
    public Shipment updateShipment(Integer id, ShipmentRequestDTO shipmentRequestDTO) {
        Shipment existingShipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found with ID: " + id));

        Supplier supplier = supplierRepository.findById(shipmentRequestDTO.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + shipmentRequestDTO.getSupplierId()));

        existingShipment.setTrackingNumber(shipmentRequestDTO.getTrackingNumber());
        existingShipment.setSupplier(supplier);

        return shipmentRepository.save(existingShipment);
    }

    @Transactional
    public Shipment updateShipmentStatus(Integer shipmentId, Shipment.ShipmentStatus newStatus, String notes) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found with ID: " + shipmentId));

        Shipment.ShipmentStatus oldStatus = shipment.getShipmentStatus();
        if (!oldStatus.equals(newStatus)) {
            shipment.setShipmentStatus(newStatus);
            Shipment updatedShipment = shipmentRepository.save(shipment);
            createShipmentStatusHistoryEntry(updatedShipment, newStatus.name(), notes != null && !notes.isEmpty() ? notes : "Status changed from " + oldStatus.name() + " to " + updatedShipment.getShipmentStatus().name());
            return updatedShipment;
        }
        return shipment;
    }

    @Transactional
    public void createShipmentStatusHistoryEntry(Shipment shipment, String statusDescription, String notes) {
        ShipmentStatusHistory history = new ShipmentStatusHistory();
        history.setShipment(shipment);
        history.setEventDatetime(LocalDateTime.now());
        history.setStatusDescription(statusDescription);
        history.setNotes(notes);
        shipmentStatusHistoryRepository.save(history);
    }

    @Transactional
    public Shipment getShipmentById(Integer id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found with ID: " + id));
    }


    @Transactional
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    @Transactional
    public void deleteShipment(Integer id) {
        if (!shipmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Shipment not found with ID: " + id);
        }
        shipmentRepository.deleteById(id);
    }

    @Transactional
    public List<ShipmentStatusHistory> getShipmentStatusHistory(Integer shipmentId) {
        if (!shipmentRepository.existsById(shipmentId)) {
            throw new EntityNotFoundException("Shipment not found with ID: " + shipmentId);
        }
        return shipmentStatusHistoryRepository.findByShipmentShipmentIdOrderByEventDatetimeAsc(shipmentId);
    }

}
