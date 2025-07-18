package com.ecomarket.backend.catalog_product.DTO.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryResponse {
    private Long productId;
    private Integer availableQuantity;
    private String location;
    private LocalDateTime lastUpdate;
}