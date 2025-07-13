package com.ecomarket.backend.catalog_product.controller;

import com.ecomarket.backend.catalog_product.DTO.InventoryOperationRequest;
import com.ecomarket.backend.catalog_product.DTO.InventoryResponse;
import com.ecomarket.backend.catalog_product.assembler.InventoryAssembler;
import com.ecomarket.backend.catalog_product.model.Inventory;
import com.ecomarket.backend.catalog_product.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryAssembler inventoryAssembler;

    @GetMapping("/{productId}")
    public EntityModel<InventoryResponse> getInventory(@PathVariable Long productId) {
        Inventory inventory = inventoryService.getInventory(productId);
        return inventoryAssembler.toModel(inventory);
    }

    @PostMapping("/{productId}/operation")
    public EntityModel<InventoryResponse> handleOperation(@PathVariable Long productId,
                                                          @Valid @RequestBody InventoryOperationRequest request) {
        Inventory updated = inventoryService.handleOperation(productId, request);
        return inventoryAssembler.toModel(updated);
    }
}
