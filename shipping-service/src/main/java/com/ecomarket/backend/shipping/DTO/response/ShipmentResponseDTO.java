package com.ecomarket.backend.shipping.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecomarket.backend.shipping.model.Shipment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponseDTO extends RepresentationModel<ShipmentResponseDTO> {

    private Integer shipmentId;
    private Integer orderId;
    private String trackingNumber;
    private LocalDateTime shipmentDate;
    private LocalDateTime estimatedDeliveryDate;
    private Shipment.ShipmentStatus shipmentStatus;
    private BigDecimal shippingCost;
    private Integer destinationAddressId;
    private SupplierResponseDTO supplier;
}
