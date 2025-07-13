package com.ecomarket.backend.cart_order.client.dto;
import lombok.Data;

@Data
public class ProductDto {
    private Long productId;
    private String name;
    private String description;
    private Double price;
    private String sku;
    private String status;
}