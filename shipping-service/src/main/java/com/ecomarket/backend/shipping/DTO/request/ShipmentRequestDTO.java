package com.ecomarket.backend.shipping.DTO.request;

import com.ecomarket.backend.shipping.model.Shipment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequestDTO {

    @NotNull(message = "Order ID cannot be null.")
    private Integer orderId;

    @NotBlank(message = "Tracking number cannot be empty.")
    private String trackingNumber;

    @NotNull(message = "Shipment date cannot be null.")
    private LocalDateTime shipmentDate;

    private LocalDateTime estimatedDeliveryDate;

    @NotNull(message = "Shipment status cannot be null.")
    private Shipment.ShipmentStatus shipmentStatus;

    @DecimalMin(value = "0.0", message = "El costo de envío no puede ser negativo.")
    private BigDecimal shippingCost;

    @NotNull(message = "Destination address ID cannot be null.")
    private Integer destinationAddressId;

    @NotNull(message = "Supplier ID cannot be null.")
    private Integer supplierId;
}
