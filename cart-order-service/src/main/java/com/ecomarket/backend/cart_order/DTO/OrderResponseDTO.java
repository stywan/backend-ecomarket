package com.ecomarket.backend.cart_order.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long id;
    private Long userId;
    private Long shippingAddressId;
    private Long paymentTransactionId;
    private LocalDateTime createdAt;
    private String orderStatus;
    private BigDecimal totalAmount;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private List<OrderItemResponseDTO> items;
}
