package com.ecomarket.backend.cart_order.DTO.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequestDTO {

    private Long userId;

    private Long paymentTransactionId;

    @NotEmpty(message = "The order must have at least one product")
    @Valid
    private List<OrderItemRequestDTO> items;
}
