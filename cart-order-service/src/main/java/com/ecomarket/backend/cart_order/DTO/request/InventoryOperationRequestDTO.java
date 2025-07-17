package com.ecomarket.backend.cart_order.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOperationRequestDTO {
    private String operationType; // Ej: "RESERVE", "RELEASE"
    private int quantity;
}