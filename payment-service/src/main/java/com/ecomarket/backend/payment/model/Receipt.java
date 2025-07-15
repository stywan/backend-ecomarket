package com.ecomarket.backend.payment.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long receiptId;

    private Long orderId;

    private Long transactionId;

    private String documentNumber;

    private LocalDateTime issueDate;

    private BigDecimal totalAmount;

    private BigDecimal taxes;

    private String customerRut;

    private String customerName;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ISSUED,
        CANCELED
    }
}
