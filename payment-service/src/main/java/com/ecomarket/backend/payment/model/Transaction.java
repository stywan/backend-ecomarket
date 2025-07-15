package com.ecomarket.backend.payment.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    private Long orderId;

    private Long userId;

    private LocalDateTime transactionDate;

    private BigDecimal amount;

    private String currency;

    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    public enum TransactionStatus {
        PENDING, APPROVED, REJECTED, REFUNDED
    }
}
