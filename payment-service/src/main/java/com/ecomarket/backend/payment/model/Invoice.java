package com.ecomarket.backend.payment.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    private Long orderId;

    private Long transactionId;

    private LocalDateTime issueDate;

    @Column(unique = true, nullable = false)
    private String documentNumber;

    private BigDecimal totalAmount;

    private BigDecimal taxes;

    private Long taxProfileId;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ISSUED, PAID, CANCELLED
    }
}
