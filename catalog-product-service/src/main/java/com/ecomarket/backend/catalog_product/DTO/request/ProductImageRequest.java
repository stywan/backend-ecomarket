package com.ecomarket.backend.catalog_product.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageRequest {
    @NotBlank(message = "Image URL must not be blank")
    private String url;
}