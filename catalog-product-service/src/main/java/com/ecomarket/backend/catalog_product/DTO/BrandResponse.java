package com.ecomarket.backend.catalog_product.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandResponse {
    private Long id;
    private String name;
    private String description;
}