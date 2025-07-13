package com.ecomarket.backend.cart_order.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnRequest {

    @NotNull(message = "Order ID must not be null")
    private Long orderId;

    @NotNull(message = "Order item ID must not be null")
    private Long orderItemId;

    @NotNull(message = "Reason must not be null")
    private String reason;
}