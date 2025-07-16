package com.ecomarket.backend.cart_order.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {
    private Long transactionId;
    private Long orderId;
    private Long userId;
    private LocalDateTime transactionDate;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String transactionStatus;
}
