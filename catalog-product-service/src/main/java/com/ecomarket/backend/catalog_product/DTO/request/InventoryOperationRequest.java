package com.ecomarket.backend.catalog_product.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryOperationRequest {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotBlank(message = "Operation type must not be blank, e.g. \"RELEASE\", \"DECREMENT\", \"INCREMENT\"")
    private String operationType; // e.g. "RESERVE", "RELEASE", "DECREMENT", "INCREMENT"

    private String location;
}