package com.ecomarket.backend.cart_order.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnStatusUpdateRequest {
    @NotNull(message = "Return status must not be null")
    private String returnStatus;
}