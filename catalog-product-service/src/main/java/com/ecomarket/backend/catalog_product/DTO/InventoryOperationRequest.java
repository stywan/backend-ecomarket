package com.ecomarket.backend.catalog_product.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryOperationRequest {
    @NotNull
    private Integer quantity;

    @NotBlank
    private String operationType; // e.g. "RESERVE", "RELEASE", "DECREMENT", "INCREMENT"

    private String location;
}