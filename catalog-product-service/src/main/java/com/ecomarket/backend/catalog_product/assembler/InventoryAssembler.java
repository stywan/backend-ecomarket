package com.ecomarket.backend.catalog_product.assembler;

import com.ecomarket.backend.catalog_product.DTO.InventoryResponse;
import com.ecomarket.backend.catalog_product.controller.InventoryController;
import com.ecomarket.backend.catalog_product.controller.ProductController;
import com.ecomarket.backend.catalog_product.model.Inventory;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InventoryAssembler implements RepresentationModelAssembler<Inventory, EntityModel<InventoryResponse>> {

    @Override
    public EntityModel<InventoryResponse> toModel(Inventory inventory) {
        InventoryResponse response = InventoryResponse.builder()
                .productId(inventory.getProduct().getId())
                .availableQuantity(inventory.getAvailableQuantity())
                .location(inventory.getLocation())
                .lastUpdate(inventory.getLastUpdate())
                .build();

        return EntityModel.of(response,
                linkTo(methodOn(InventoryController.class).getInventory(inventory.getProduct().getId())).withSelfRel(),
                linkTo(methodOn(InventoryController.class).handleOperation(inventory.getProduct().getId(), null)).withRel("operation"),
                linkTo(methodOn(ProductController.class).getProduct(inventory.getProduct().getId())).withRel("product")
        );
    }
}