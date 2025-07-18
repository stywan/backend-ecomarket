package com.ecomarket.backend.shipping.controller;

import com.ecomarket.backend.shipping.DTO.request.ShipmentRequestDTO;
import com.ecomarket.backend.shipping.DTO.response.ShipmentResponseDTO;
import com.ecomarket.backend.shipping.DTO.response.ShipmentStatusHistoryResponseDTO;
import com.ecomarket.backend.shipping.assembler.ShipmentAssembler;
import com.ecomarket.backend.shipping.assembler.ShipmentStatusHistoryAssembler;
import com.ecomarket.backend.shipping.model.Shipment;
import com.ecomarket.backend.shipping.model.ShipmentStatusHistory;
import com.ecomarket.backend.shipping.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/shipments")
@Tag(name = "Shipments", description = "Operaciones para la gestión y seguimiento de envíos.")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentAssembler shipmentAssembler;
    private final ShipmentStatusHistoryAssembler historyAssembler;

    @Operation(summary = "Crear un nuevo envío",
            description = "Registra un nuevo envío con su información inicial y genera un número de seguimiento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envío creado exitosamente.",
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = ShipmentResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de creación exitosa",
                                    value = "{\"shipmentId\":1,\"orderId\":101,\"trackingNumber\":\"ECO-20250717-001\",\"shipmentDate\":\"2025-07-17T18:00:00\",\"estimatedDeliveryDate\":\"2025-07-20T18:00:00\",\"shipmentStatus\":\"PENDING\",\"shippingCost\":3990.0,\"destinationAddressId\":1,\"supplier\":{\"supplierId\":1,\"name\":\"Proveedor Rápido\",\"contactPerson\":\"Juan Pérez\",\"phone\":\"+56912345678\",\"email\":\"juan.perez@rapido.com\"},\"_links\":{\"self\":{\"href\":\"http://localhost:8080/api/v1/shipments/1\"},\"statusHistory\":{\"href\":\"http://localhost:8080/api/v1/shipments/1/history\"},\"updateStatus\":{\"href\":\"http://localhost:8080/api/v1/shipments/1/status{?newStatus,notes}\",\"templated\":true},\"supplier\":{\"href\":\"http://localhost:8080/api/v1/shipments/suppliers/1\"}}}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes o incorrectos, ID de orden/proveedor no encontrado).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Order not found with ID: 101\"}"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<ShipmentResponseDTO>> createShipment(@Valid @RequestBody ShipmentRequestDTO shipmentRequestDTO) {
        Shipment newShipment = shipmentService.createShipment(shipmentRequestDTO);
        return new ResponseEntity<>(shipmentAssembler.toModel(newShipment), HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener envío por ID",
            description = "Recupera los detalles completos de un envío específico, incluyendo su estado actual y enlaces HATEOAS para navegación.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envío encontrado exitosamente.",
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = ShipmentResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de envío",
                                    value = "{\"shipmentId\":1,\"orderId\":101,\"trackingNumber\":\"ECO-20250717-001\",\"shipmentDate\":\"2025-07-17T18:00:00\",\"estimatedDeliveryDate\":\"2025-07-20T18:00:00\",\"shipmentStatus\":\"PENDING\",\"shippingCost\":3990.0,\"destinationAddressId\":1,\"supplier\":{\"supplierId\":1,\"name\":\"Proveedor Rápido\",\"contactPerson\":\"Juan Pérez\",\"phone\":\"+56912345678\",\"email\":\"juan.perez@rapido.com\"},\"_links\":{\"self\":{\"href\":\"http://localhost:8080/api/v1/shipments/1\"},\"statusHistory\":{\"href\":\"http://localhost:8080/api/v1/shipments/1/history\"},\"supplier\":{\"href\":\"http://localhost:8080/api/v1/shipments/suppliers/1\"}}}"
                            ))),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado para el ID proporcionado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Shipment not found with ID: 1\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ShipmentResponseDTO>> getShipmentById(
            @Parameter(description = "ID del envío a buscar.", example = "1")
            @PathVariable Integer id) {
        Shipment shipment = shipmentService.getShipmentById(id);
        return ResponseEntity.ok(shipmentAssembler.toModel(shipment));
    }

    @Operation(summary = "Obtener todos los envíos",
            description = "Recupera una lista de todos los envíos existentes, con enlaces HATEOAS para cada envío y la colección.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de envíos recuperada exitosamente.",
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = CollectionModel.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ShipmentResponseDTO>>> getAllShipments() {
        List<Shipment> shipments = shipmentService.getAllShipments();
        return ResponseEntity.ok(shipmentAssembler.toCollectionModel(shipments));
    }

    @Operation(summary = "Actualizar un envío",
            description = "Actualiza los detalles de un envío existente, como el número de seguimiento o el proveedor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envío actualizado exitosamente.",
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = ShipmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos incorrectos).", content = @Content),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado para el ID proporcionado.", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ShipmentResponseDTO>> updateShipment(
            @Parameter(description = "ID del envío a actualizar.", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody ShipmentRequestDTO shipmentRequestDTO) {
        Shipment updatedShipment = shipmentService.updateShipment(id, shipmentRequestDTO);
        return ResponseEntity.ok(shipmentAssembler.toModel(updatedShipment));
    }


    @Operation(summary = "Actualizar el estado de un envío",
            description = "Cambia el estado de un envío específico y registra el cambio en el historial.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del envío actualizado exitosamente.",
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = ShipmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Estado proporcionado no es válido.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado para el ID proporcionado.", content = @Content)
    })
    @PatchMapping("/{shipmentId}/status")
    public ResponseEntity<EntityModel<ShipmentResponseDTO>> updateShipmentStatus(
            @Parameter(description = "ID del envío cuyo estado se va a actualizar.", example = "1")
            @PathVariable Integer shipmentId,
            @Parameter(description = "El nuevo estado del envío.", required = true,
                    schema = @Schema(type = "string", allowableValues = {"PENDING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "CANCELLED"}))
            @RequestParam Shipment.ShipmentStatus newStatus,
            @Parameter(description = "Notas adicionales sobre el cambio de estado.")
            @RequestParam(required = false) String notes) {
        Shipment updatedShipment = shipmentService.updateShipmentStatus(shipmentId, newStatus, notes);
        return ResponseEntity.ok(shipmentAssembler.toModel(updatedShipment));
    }

    @Operation(summary = "Eliminar un envío",
            description = "Elimina un envío del sistema permanentemente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Envío eliminado exitosamente (No Content).", content = @Content),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado para el ID proporcionado.", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShipment(
            @Parameter(description = "ID del envío a eliminar.", example = "1")
            @PathVariable Integer id) {
        shipmentService.deleteShipment(id);
    }


    @Operation(summary = "Obtener historial de estado de un envío",
            description = "Recupera la cronología de cambios de estado para un envío específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de estado recuperado exitosamente.",
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = CollectionModel.class))),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado para el ID proporcionado.", content = @Content)
    })
    @GetMapping("/{shipmentId}/history")
    public ResponseEntity<CollectionModel<EntityModel<ShipmentStatusHistoryResponseDTO>>> getShipmentStatusHistory(
            @Parameter(description = "ID del envío para el cual se desea obtener el historial de estado.", example = "1")
            @PathVariable Integer shipmentId) {
        List<ShipmentStatusHistory> historyEntries = shipmentService.getShipmentStatusHistory(shipmentId);

        CollectionModel<EntityModel<ShipmentStatusHistoryResponseDTO>> collectionModel = historyAssembler.toCollectionModel(historyEntries);

        collectionModel.add(linkTo(methodOn(ShipmentController.class).getShipmentStatusHistory(shipmentId)).withSelfRel());
        collectionModel.add(linkTo(methodOn(ShipmentController.class).getShipmentById(shipmentId)).withRel("shipment"));

        return ResponseEntity.ok(collectionModel);
    }
}