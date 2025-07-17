package com.ecomarket.backend.catalog_product.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageResponse {
    private Long id;
    private String url;
}
