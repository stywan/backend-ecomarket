package com.ecomarket.backend.cart_order.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private LocalDateTime orderDate;
    private String orderStatus;
    private Double subtotal;
    private Double totalAmount;
    private Double shippingCost;
    private List<OrderItemResponse> items;
}
