package com.ecomarket.backend.cart_order.DTO.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {
    private Long productId;
    private Integer quantity;
    private Double unitSalePrice;
}