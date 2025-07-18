package com.ecomarket.backend.shipping.DTO.request;

import jakarta.validation.constraints.NotNull;


import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequestDTO {

    @NotNull(message = "Order ID cannot be null.")
    private Long orderId; // Asumo que el orderId es Long, ajusta si es Integer

    @NotNull(message = "Tracking number cannot be null.")
    @Size(min = 5, max = 50, message = "Tracking number must be between 5 and 50 characters.") // Ejemplo de validación
    private String trackingNumber;

    @NotNull(message = "Supplier ID cannot be null.")
    private Integer supplierId;
}
