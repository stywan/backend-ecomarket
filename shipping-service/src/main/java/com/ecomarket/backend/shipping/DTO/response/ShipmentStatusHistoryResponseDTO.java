package com.ecomarket.backend.shipping.DTO.response;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusHistoryResponseDTO extends RepresentationModel<ShipmentStatusHistoryResponseDTO> {
    private Integer historyId;
    private Integer shipmentId;
    private LocalDateTime eventDatetime;
    private String statusDescription;
    private String notes;
}
