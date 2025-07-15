package com.ecomarket.backend.cart_order.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private String categoryName;
    private String brandName;
    private BigDecimal weight;
    private String dimensions;
    private String status;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;
    private List<String> imageUrls;

}
