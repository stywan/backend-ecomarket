package com.ecomarket.backend.auth.controller;

import com.ecomarket.backend.auth.DTO.LoginRequest;
import com.ecomarket.backend.auth.DTO.LoginResponse;
import com.ecomarket.backend.auth.DTO.RegisterRequest;
import com.ecomarket.backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API para registro de usuarios y login") // Etiqueta para agrupar en Swagger UI
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar un nuevo usuario",
            description = "Crea una nueva cuenta de usuario en el sistema. Requiere un email único y una contraseña. Devuelve un token JWT para la sesión iniciada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado y sesión iniciada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. email ya registrado, datos faltantes o incorrectos)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Bad Request\", \"message\": \"Email already exists\"}")))
    })
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Iniciar sesión de usuario",
            description = "Autentica al usuario con sus credenciales y devuelve un token de acceso (JWT).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas (email o contraseña incorrectos)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Unauthorized\", \"message\": \"Bad credentials\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}