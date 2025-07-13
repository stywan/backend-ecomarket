package com.ecomarket.backend.cart_order.DTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private Long productId;
    private Integer quantity;
    private Double unitPrice;
}