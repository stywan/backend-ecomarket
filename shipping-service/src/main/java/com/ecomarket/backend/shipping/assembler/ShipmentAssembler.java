package com.ecomarket.backend.shipping.assembler;

import com.ecomarket.backend.shipping.DTO.response.ShipmentResponseDTO;
import com.ecomarket.backend.shipping.DTO.response.SupplierResponseDTO;
import com.ecomarket.backend.shipping.controller.ShipmentController;
import com.ecomarket.backend.shipping.model.Shipment;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ShipmentAssembler implements RepresentationModelAssembler<Shipment, EntityModel<ShipmentResponseDTO>> {

    @Override
    public EntityModel<ShipmentResponseDTO> toModel(Shipment shipment) {
        ShipmentResponseDTO response = new ShipmentResponseDTO();
        response.setShipmentId(shipment.getShipmentId());
        response.setOrderId(shipment.getOrderId());
        response.setTrackingNumber(shipment.getTrackingNumber());
        response.setShipmentDate(shipment.getShipmentDate());
        response.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        response.setShipmentStatus(shipment.getShipmentStatus());
        response.setShippingCost(shipment.getShippingCost());
        response.setDestinationAddressId(shipment.getDestinationAddressId());

        if (shipment.getSupplier() != null) {
            response.setSupplier(new SupplierResponseDTO(
                    shipment.getSupplier().getSupplierId(),
                    shipment.getSupplier().getName(),
                    shipment.getSupplier().getContactPerson(),
                    shipment.getSupplier().getPhone(),
                    shipment.getSupplier().getEmail()
            ));
        }

        // Crear EntityModel y añadir enlaces
        return EntityModel.of(response,
                linkTo(methodOn(ShipmentController.class).getShipmentById(shipment.getShipmentId())).withSelfRel(),
                linkTo(methodOn(ShipmentController.class).getShipmentStatusHistory(shipment.getShipmentId())).withRel("statusHistory")
        );
    }
}