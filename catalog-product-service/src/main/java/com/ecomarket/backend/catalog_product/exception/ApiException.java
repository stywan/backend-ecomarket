package com.ecomarket.backend.catalog_product.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiException {
    private final String message;
    private final String debugMessage;
    private final int status;
    private final LocalDateTime timestamp = LocalDateTime.now();
}
