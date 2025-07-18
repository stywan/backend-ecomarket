package com.ecomarket.backend.shipping.assembler;

import com.ecomarket.backend.shipping.DTO.response.ShipmentStatusHistoryResponseDTO;
import com.ecomarket.backend.shipping.controller.ShipmentController;
import com.ecomarket.backend.shipping.model.ShipmentStatusHistory;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ShipmentStatusHistoryAssembler implements RepresentationModelAssembler<ShipmentStatusHistory, EntityModel<ShipmentStatusHistoryResponseDTO>> {

    @Override
    public EntityModel<ShipmentStatusHistoryResponseDTO> toModel(ShipmentStatusHistory history) {
        ShipmentStatusHistoryResponseDTO response = new ShipmentStatusHistoryResponseDTO(
                history.getHistoryId(),
                history.getShipment().getShipmentId(),
                history.getEventDatetime(),
                history.getStatusDescription(),
                history.getNotes()
        );
        return EntityModel.of(response,
                linkTo(methodOn(ShipmentController.class).getShipmentById(history.getShipment().getShipmentId())).withRel("shipment"),
                linkTo(methodOn(ShipmentController.class).getShipmentStatusHistory(history.getShipment().getShipmentId())).withRel("allHistoryForThisShipment")
        );
    }
}