package com.ecomarket.backend.shipping.assembler;
import com.ecomarket.backend.shipping.DTO.response.SupplierResponseDTO;
import com.ecomarket.backend.shipping.controller.SupplierController;
import com.ecomarket.backend.shipping.model.Supplier;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SupplierAssembler implements RepresentationModelAssembler<Supplier, EntityModel<SupplierResponseDTO>> {

    @Override
    public EntityModel<SupplierResponseDTO> toModel(Supplier supplier) {
        SupplierResponseDTO response = new SupplierResponseDTO(
                supplier.getSupplierId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail()
        );
        return EntityModel.of(response,
                linkTo(methodOn(SupplierController.class).getSupplierById(supplier.getSupplierId())).withSelfRel(),
                linkTo(methodOn(SupplierController.class).getAllSuppliers()).withRel("allSuppliers")
        );
    }
}
