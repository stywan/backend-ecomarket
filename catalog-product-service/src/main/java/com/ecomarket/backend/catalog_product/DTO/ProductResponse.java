package com.ecomarket.backend.catalog_product.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private BigDecimal weight;
    private String dimensions;
    private String status;

    private CategoryResponse category;
    private BrandResponse brand;
    private List<ProductImageResponse> images;
}

