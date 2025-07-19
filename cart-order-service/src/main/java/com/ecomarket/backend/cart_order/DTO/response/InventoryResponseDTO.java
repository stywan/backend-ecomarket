package com.ecomarket.backend.cart_order.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponseDTO {
    private Long id;
    private Long productId;
    private int availableQuantity;
    private LocalDateTime lastUpdate;
}
