package com.ecomarket.backend.cart_order.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDTO {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
}
