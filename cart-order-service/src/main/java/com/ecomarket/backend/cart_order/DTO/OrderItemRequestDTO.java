package com.ecomarket.backend.cart_order.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OrderItemRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 1, message = "The amount must be greater than 0")
    private int quantity;
}
