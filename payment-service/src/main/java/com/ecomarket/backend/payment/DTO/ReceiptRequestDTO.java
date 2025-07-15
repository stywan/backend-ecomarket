package com.ecomarket.backend.payment.DTO;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptRequestDTO {
    private Long orderId;
    private Long transactionId;
    private String documentNumber;
    private BigDecimal totalAmount;
    private BigDecimal taxes;
    private String customerRut;
    private String customerName;
}
