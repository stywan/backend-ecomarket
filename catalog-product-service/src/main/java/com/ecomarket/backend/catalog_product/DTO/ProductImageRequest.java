package com.ecomarket.backend.catalog_product.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductImageRequest {
    @NotBlank
    private String url;
}