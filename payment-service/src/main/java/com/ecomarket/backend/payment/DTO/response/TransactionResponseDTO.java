package com.ecomarket.backend.payment.DTO.response;

import lombok.*;

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
