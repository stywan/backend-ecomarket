package com.ecomarket.backend.auth.controller;
import com.ecomarket.backend.auth.DTO.PasswordUpdateRequest;
import com.ecomarket.backend.auth.DTO.UserResponse;
import com.ecomarket.backend.auth.DTO.UserUpdateRequest;
import com.ecomarket.backend.auth.assemblers.UserAssembler;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.security.AuthenticatedUserProvider;
import com.ecomarket.backend.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API para gestionar la información del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserAssembler userAssembler;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Operation(summary = "Obtener el perfil del usuario",
            description = "Recupera la información del perfil del usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil del usuario recuperado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EntityModel.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/me")
    public EntityModel<UserResponse> getProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        UserResponse response = userService.getUserProfile(realUser);
        return userAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar el perfil del usuario",
            description = "Permite al usuario autenticado actualizar su información de perfil (nombre, apellido, email).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil del usuario actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EntityModel.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. email ya en uso, datos incorrectos)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/me")
    public EntityModel<UserResponse> updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        userService.updateProfile(realUser, request);
        UserResponse response = userService.getUserProfile(realUser);
        return userAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar la contraseña del usuario",
            description = "Permite al usuario autenticado cambiar su contraseña, requiriendo la contraseña actual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada con éxito"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. contraseña actual incorrecta, nueva contraseña no cumple requisitos)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordUpdateRequest request) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        userService.updatePassword(realUser, request);
        return ResponseEntity.noContent()
                .header("X-Message", "Contraseña actualizada con éxito")
                .build();
    }
}