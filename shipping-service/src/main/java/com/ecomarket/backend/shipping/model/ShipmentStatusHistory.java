package com.ecomarket.backend.shipping.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipment_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @ManyToOne
    @JoinColumn(name = "shipment_id", referencedColumnName = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "event_datetime", nullable = false)
    private LocalDateTime eventDatetime;

    @Column(name = "status_description", nullable = false)
    private String statusDescription;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}