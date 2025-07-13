package com.ecomarket.backend.catalog_product.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductImageRequest {
    @NotBlank(message = "Image URL must not be blank")
    private String url;
}