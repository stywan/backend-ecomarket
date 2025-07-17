package com.ecomarket.backend.shipping.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Integer shipmentId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId; // Virtual FK to Order Service

    @Column(name = "tracking_number", unique = true, nullable = false)
    private String trackingNumber;

    @Column(name = "shipment_date", nullable = false)
    private LocalDateTime shipmentDate;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status", nullable = false)
    private ShipmentStatus shipmentStatus;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;

    @Column(name = "destination_address_id", nullable = false)
    private Integer destinationAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", referencedColumnName = "supplier_id", nullable = false)
    private Supplier supplier;

    public enum ShipmentStatus {
        PENDING,
        IN_ROUTE,
        OUT_FOR_DELIVERY,
        DELIVERED,
        FAILED
    }
}
