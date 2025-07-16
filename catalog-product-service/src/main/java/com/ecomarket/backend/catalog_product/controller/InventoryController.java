package com.ecomarket.backend.catalog_product.controller;

import com.ecomarket.backend.catalog_product.DTO.InventoryOperationRequest;
import com.ecomarket.backend.catalog_product.DTO.InventoryResponse;
import com.ecomarket.backend.catalog_product.assembler.InventoryAssembler;
import com.ecomarket.backend.catalog_product.model.Inventory;
import com.ecomarket.backend.catalog_product.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "Gestión de inventarios de productos")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryAssembler inventoryAssembler;

    @GetMapping("/{productId}")
    @Operation(summary = "Obtener inventario por producto", description = "Devuelve el inventario del producto según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario encontrado"),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado para el producto")
    })
    public ResponseEntity<EntityModel<InventoryResponse>> getInventory(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long productId) {
        Inventory inventory = inventoryService.getInventory(productId);
        if (inventory == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(inventoryAssembler.toModel(inventory));
    }

    @PostMapping("/{productId}/operation")
    @Operation(summary = "Realizar operación de inventario", description = "Registra una operación (entrada o salida) sobre el inventario del producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operación realizada y inventario actualizado"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Inventario o producto no encontrado")
    })
    public ResponseEntity<EntityModel<InventoryResponse>> handleOperation(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long productId,
            @Parameter(description = "Datos de la operación a realizar", required = true) @Valid @RequestBody InventoryOperationRequest request) {
        Inventory updated = inventoryService.handleOperation(productId, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(inventoryAssembler.toModel(updated));
    }
}
