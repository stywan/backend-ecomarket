package com.ecomarket.backend.catalog_product.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;

    @NotBlank
    private String sku;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long brandId;

    private BigDecimal weight;
    private String dimensions;
}
