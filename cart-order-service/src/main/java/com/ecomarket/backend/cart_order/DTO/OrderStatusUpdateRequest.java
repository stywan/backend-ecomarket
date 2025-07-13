package com.ecomarket.backend.cart_order.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    @NotNull(message = "Order status must not be null")
    private String orderStatus;
}