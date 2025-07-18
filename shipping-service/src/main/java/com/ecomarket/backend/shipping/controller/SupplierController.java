package com.ecomarket.backend.shipping.controller;

import com.ecomarket.backend.shipping.DTO.request.SupplierRequestDTO;
import com.ecomarket.backend.shipping.DTO.response.SupplierResponseDTO;
import com.ecomarket.backend.shipping.assembler.SupplierAssembler;
import com.ecomarket.backend.shipping.model.Supplier;
import com.ecomarket.backend.shipping.service.SupplierService;
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

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/suppliers")
@Tag(name = "Suppliers", description = "Operaciones para la gestión de proveedores.")
public class SupplierController {

    private final SupplierService supplierService;
    private final SupplierAssembler supplierAssembler;

    @Operation(summary = "Crear un nuevo proveedor",
            description = "Registra un nuevo proveedor con su información de contacto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Proveedor creado exitosamente.",
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = SupplierResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de creación exitosa",
                                    value = "{\"supplierId\":1,\"name\":\"Nuevo Distribuidor\",\"contactPerson\":\"Maria Lopez\",\"phone\":\"+56998765432\",\"email\":\"maria.lopez@distribuidor.com\",\"_links\":{\"self\":{\"href\":\"http://localhost:8080/api/v1/suppliers/1\"},\"allSuppliers\":{\"href\":\"http://localhost:8080/api/v1/suppliers\"}}}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes o incorrectos).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Validation error: name must not be blank\"}"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<SupplierResponseDTO>> createSupplier(@Valid @RequestBody SupplierRequestDTO supplierRequestDTO) {
        Supplier supplierToCreate = mapToSupplierEntity(supplierRequestDTO);
        Supplier newSupplier = supplierService.createSupplier(supplierToCreate);
        return new ResponseEntity<>(supplierAssembler.toModel(newSupplier), HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener proveedor por ID",
            description = "Recupera los detalles completos de un proveedor específico por su identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado exitosamente.",
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = SupplierResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de proveedor",
                                    value = "{\"supplierId\":1,\"name\":\"Proveedor Principal\",\"contactPerson\":\"Pedro Gómez\",\"phone\":\"+56911223344\",\"email\":\"pedro.gomez@principal.com\",\"_links\":{\"self\":{\"href\":\"http://localhost:8080/api/v1/suppliers/1\"},\"allSuppliers\":{\"href\":\"http://localhost:8080/api/v1/suppliers\"}}}"
                            ))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado para el ID proporcionado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Supplier not found with ID: 1\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<SupplierResponseDTO>> getSupplierById(
            @Parameter(description = "ID del proveedor a buscar.", example = "1")
            @PathVariable Integer id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplierAssembler.toModel(supplier));
    }

    @Operation(summary = "Obtener todos los proveedores",
            description = "Recupera una lista de todos los proveedores existentes en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de proveedores recuperada exitosamente.",
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = CollectionModel.class))) // CollectionModel para HATEOAS
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<SupplierResponseDTO>>> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(supplierAssembler.toCollectionModel(suppliers));
    }

    @Operation(summary = "Actualizar un proveedor",
            description = "Actualiza los detalles de un proveedor existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor actualizado exitosamente.",
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = SupplierResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos incorrectos).", content = @Content),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado para el ID proporcionado.", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<SupplierResponseDTO>> updateSupplier(
            @Parameter(description = "ID del proveedor a actualizar.", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody SupplierRequestDTO supplierRequestDTO) {
        Supplier supplierToUpdate = mapToSupplierEntity(supplierRequestDTO);
        Supplier updatedSupplier = supplierService.updateSupplier(id, supplierToUpdate);
        return ResponseEntity.ok(supplierAssembler.toModel(updatedSupplier));
    }

    @Operation(summary = "Eliminar un proveedor",
            description = "Elimina un proveedor del sistema permanentemente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Proveedor eliminado exitosamente (No Content).", content = @Content),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado para el ID proporcionado.", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSupplier(
            @Parameter(description = "ID del proveedor a eliminar.", example = "1")
            @PathVariable Integer id) {
        supplierService.deleteSupplier(id);
    }

    private Supplier mapToSupplierEntity(SupplierRequestDTO dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setPhone(dto.getPhone());
        supplier.setEmail(dto.getEmail());
        return supplier;
    }
}