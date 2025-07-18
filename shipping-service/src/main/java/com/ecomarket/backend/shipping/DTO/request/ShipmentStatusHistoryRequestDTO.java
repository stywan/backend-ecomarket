package com.ecomarket.backend.shipping.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusHistoryRequestDTO {

    @NotNull(message = "Shipment ID cannot be null.")
    private Integer shipmentId;

    @NotNull(message = "Event datetime cannot be null.")
    private LocalDateTime eventDatetime;

    @NotBlank(message = "Status description cannot be empty.")
    @Size(max = 255)
    private String statusDescription;

    private String notes;
}
