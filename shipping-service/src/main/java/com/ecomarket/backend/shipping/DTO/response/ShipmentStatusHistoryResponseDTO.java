package com.ecomarket.backend.shipping.DTO.response;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusHistoryResponseDTO {
    private Integer historyId;
    private Integer shipmentId;
    private LocalDateTime eventDatetime;
    private String statusDescription;
    private String notes;
}
