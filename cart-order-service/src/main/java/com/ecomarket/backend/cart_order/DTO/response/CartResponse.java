package com.ecomarket.backend.cart_order.DTO.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {
    private Long cartId;
    private Long userId;
    private String status;
    private List<CartItemResponse> items;
}