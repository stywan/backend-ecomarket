package com.ecomarket.backend.auth.controller;

import com.ecomarket.backend.auth.DTO.FiscalProfileRequest;
import com.ecomarket.backend.auth.DTO.FiscalProfileResponse;
import com.ecomarket.backend.auth.assemblers.FiscalProfileAssembler;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.security.AuthenticatedUserProvider;
import com.ecomarket.backend.auth.service.FiscalProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/fiscal-profiles")
@RequiredArgsConstructor
@Tag(name = "Fiscal Profiles", description = "API para gestionar los perfiles fiscales de los usuarios") // Etiqueta para agrupar
@SecurityRequirement(name = "bearerAuth") // Requiere autenticación JWT para todos los métodos
public class FiscalProfileController {

    private final FiscalProfileService fiscalProfileService;
    private final FiscalProfileAssembler fiscalProfileAssembler;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Operation(summary = "Añadir o actualizar un perfil fiscal",
            description = "Crea un nuevo perfil fiscal para el usuario autenticado o actualiza uno existente basado en el RUT. Si el RUT ya existe, se actualiza ese perfil; de lo contrario, se crea uno nuevo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil fiscal añadido/actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EntityModel.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes o incorrectos)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public EntityModel<FiscalProfileResponse> addOrUpdateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FiscalProfileRequest request) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        FiscalProfileResponse response = fiscalProfileService.addOrUpdateFiscalProfile(realUser, request);
        return fiscalProfileAssembler.toModel(response);
    }

    @Operation(summary = "Obtener perfiles fiscales del usuario",
            description = "Recupera una lista de todos los perfiles fiscales asociados al usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de perfiles fiscales recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CollectionModel.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public CollectionModel<EntityModel<FiscalProfileResponse>> listProfiles(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        List<EntityModel<FiscalProfileResponse>> profiles = fiscalProfileService.listUserProfiles(realUser)
                .stream()
                .map(fiscalProfileAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(profiles);
    }
}