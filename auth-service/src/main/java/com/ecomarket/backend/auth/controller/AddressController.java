package com.ecomarket.backend.auth.controller;
import com.ecomarket.backend.auth.DTO.AddressRequest;
import com.ecomarket.backend.auth.DTO.AddressResponse;
import com.ecomarket.backend.auth.assemblers.AddressAssembler;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.security.AuthenticatedUserProvider;
import com.ecomarket.backend.auth.service.AddressService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "API para gestionar las direcciones de los usuarios")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;
    private final AddressAssembler addressAssembler;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Operation(summary = "Añadir una nueva dirección",
            description = "Permite a un usuario autenticado añadir una nueva dirección a su perfil.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección añadida exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EntityModel.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes o incorrectos)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public EntityModel<AddressResponse> addAddress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        AddressResponse response = addressService.addAddress(realUser, request);
        return addressAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar una dirección existente",
            description = "Actualiza una dirección específica del usuario autenticado por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección actualizada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EntityModel.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No autenticado o no autorizado (la dirección no pertenece al usuario)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}")
    public EntityModel<AddressResponse> updateAddress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID de la dirección a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        AddressResponse response = addressService.updateAddress(realUser, id, request);
        return addressAssembler.toModel(response);
    }

    @Operation(summary = "Eliminar una dirección",
            description = "Elimina una dirección específica del usuario autenticado por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado o no autorizado (la dirección no pertenece al usuario)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada",
                    content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    public void deleteAddress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID de la dirección a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        addressService.deleteAddress(realUser, id);
    }

    @Operation(summary = "Obtener todas las direcciones del usuario",
            description = "Recupera una lista de todas las direcciones asociadas al usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de direcciones recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CollectionModel.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public CollectionModel<EntityModel<AddressResponse>> getAllAddresses(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        List<EntityModel<AddressResponse>> addresses = addressService.getUserAddresses(realUser)
                .stream()
                .map(addressAssembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(addresses);
    }
}