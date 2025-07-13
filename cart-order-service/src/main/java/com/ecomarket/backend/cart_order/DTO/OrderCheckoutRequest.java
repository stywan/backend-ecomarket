package com.ecomarket.backend.cart_order.DTO;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCheckoutRequest {
    @NotNull(message = "Shipping address ID must not be null")
    private Long shippingAddressId;

    @NotNull(message = "Tax profile ID must not be null")
    private Long taxProfileId;
}
