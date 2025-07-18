package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.request.TransactionRequestDTO;
import com.ecomarket.backend.payment.DTO.response.TransactionResponseDTO;
import com.ecomarket.backend.payment.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Operaciones para la gestión de transacciones")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Crear una nueva transacción",
            description = "Registra una nueva transacción con sus detalles. Retorna la transacción creada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transacción creada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de creación exitosa",
                                    value = "{\"transactionId\":1,\"userId\":10,\"amount\":50000.0,\"currency\":\"CLP\",\"status\":\"PENDING\",\"transactionDate\":\"2025-07-18T10:30:00\"}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes o incorrectos).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Validation error: userId must not be null\"}"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO request) {
        return ResponseEntity.ok(transactionService.createTransaction(request));
    }

    @Operation(summary = "Obtener transacciones por usuario o estado",
            description = "Recupera una lista de transacciones. Puede filtrar por ID de usuario o por estado de la transacción. Si no se proporcionan filtros, devuelve 400 Bad Request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de transacciones encontrada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponseDTO.class, type = "array"),
                            examples = {
                                    @ExampleObject(name = "Transacciones por usuario",
                                            value = "[{\"transactionId\":1,\"userId\":10,\"amount\":50000.0,\"currency\":\"CLP\",\"status\":\"PENDING\",\"transactionDate\":\"2025-07-18T10:30:00\"}]"),
                                    @ExampleObject(name = "Transacciones por estado",
                                            value = "[{\"transactionId\":2,\"userId\":11,\"amount\":25000.0,\"currency\":\"CLP\",\"status\":\"COMPLETED\",\"transactionDate\":\"2025-07-17T15:00:00\"}]")
                            })),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida: Se requiere al menos un parámetro (userId o status).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Se requiere al menos un parámetro (userId o status).\"}"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions(
            @Parameter(description = "ID del usuario para filtrar transacciones.", example = "10", required = false)
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Estado de la transacción para filtrar (ej. PENDING, APPROVED, REJECTED, REFUNDED).", example = "PENDING", required = false)
            @RequestParam(required = false) String status
    ) {
        if (userId != null && status != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByUserAndStatus(userId, status));
        } else if (userId != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByUser(userId));
        } else if (status != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByStatus(status));
        } else {
            // Manejo del caso donde no se proporciona ningún parámetro
            return ResponseEntity.badRequest().body(List.of()); // Devuelve una lista vacía con 400
        }
    }

    @Operation(summary = "Actualizar el estado de una transacción",
            description = "Cambia el estado de una transacción específica. Retorna la transacción actualizada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de la transacción actualizado exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de actualización de estado",
                                    value = "{\"transactionId\":1,\"userId\":10,\"amount\":50000.0,\"currency\":\"CLP\",\"status\":\"COMPLETED\",\"transactionDate\":\"2025-07-18T10:30:00\"}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. estado no válido).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Invalid status value: INVALID_STATE\"}"))),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada para el ID proporcionado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Transaction not found with ID: 1\"}"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @PatchMapping("/{transactionId}/status")
    public ResponseEntity<TransactionResponseDTO> updateStatus(
            @Parameter(description = "ID de la transacción a actualizar.", example = "1")
            @PathVariable Long transactionId,
            @Parameter(description = "Nuevo estado de la transacción (ej. PENDING, APPROVED, REJECTED, REFUNDED).", example = "APPROVED")
            @RequestParam String newStatus) {
        return ResponseEntity.ok(transactionService.updateTransactionStatus(transactionId, newStatus));
    }
}
