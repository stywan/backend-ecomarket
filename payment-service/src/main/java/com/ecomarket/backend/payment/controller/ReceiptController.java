package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.request.ReceiptRequestDTO;
import com.ecomarket.backend.payment.DTO.response.ReceiptResponseDTO;
import com.ecomarket.backend.payment.service.ReceiptService;
import jakarta.validation.Valid;
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


@RestController
@RequestMapping("/api/v1/receipts")
@Tag(name = "Receipts", description = "Operaciones para la gestión de recibos/boletas")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @Operation(summary = "Crear un nuevo recibo",
            description = "Registra un nuevo recibo o boleta con sus detalles. Retorna el recibo creado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recibo creado exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReceiptResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de creación exitosa",
                                    value = "{\"receiptId\":1,\"transactionId\":101,\"orderId\":201,\"customerRut\":\"12345678-9\",\"issueDate\":\"2025-07-18T14:00:00\",\"totalAmount\":15000.0,\"currency\":\"CLP\"}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes o incorrectos).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Validation error: transactionId must not be null\"}"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ReceiptResponseDTO> createReceipt(@Valid @RequestBody ReceiptRequestDTO request) {
        return ResponseEntity.ok(receiptService.createReceipt(request));
    }

    // ... (resto del controlador sin cambios)

    @Operation(summary = "Obtener recibos por ID de transacción",
            description = "Recupera una lista de recibos asociados a un ID de transacción específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de recibos encontrada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReceiptResponseDTO.class, type = "array"),
                            examples = @ExampleObject(
                                    name = "Ejemplo de lista de recibos por transacción",
                                    value = "[{\"receiptId\":1,\"transactionId\":101,\"orderId\":201,\"customerRut\":\"12345678-9\",\"issueDate\":\"2025-07-18T14:00:00\",\"totalAmount\":15000.0,\"currency\":\"CLP\"}]"
                            ))),
            @ApiResponse(responseCode = "404", description = "No se encontraron recibos para el ID de transacción proporcionado.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @GetMapping("/by-transaction/{transactionId}")
    public ResponseEntity<List<ReceiptResponseDTO>> getByTransaction(
            @Parameter(description = "ID de la transacción para buscar recibos.", example = "101")
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(receiptService.getByTransactionId(transactionId));
    }

    @Operation(summary = "Obtener recibos por ID de orden",
            description = "Recupera una lista de recibos asociados a un ID de orden de compra específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de recibos encontrada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReceiptResponseDTO.class, type = "array"),
                            examples = @ExampleObject(
                                    name = "Ejemplo de lista de recibos por orden",
                                    value = "[{\"receiptId\":1,\"transactionId\":101,\"orderId\":201,\"customerRut\":\"12345678-9\",\"issueDate\":\"2025-07-18T14:00:00\",\"totalAmount\":15000.0,\"currency\":\"CLP\"}]"
                            ))),
            @ApiResponse(responseCode = "404", description = "No se encontraron recibos para el ID de orden proporcionado.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<ReceiptResponseDTO>> getByOrder(
            @Parameter(description = "ID de la orden de compra para buscar recibos.", example = "201")
            @PathVariable Long orderId) {
        return ResponseEntity.ok(receiptService.getByOrderId(orderId));
    }

    @Operation(summary = "Obtener recibos por RUT del cliente",
            description = "Recupera una lista de recibos asociados al RUT (Rol Único Tributario) de un cliente específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de recibos encontrada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReceiptResponseDTO.class, type = "array"),
                            examples = @ExampleObject(
                                    name = "Ejemplo de lista de recibos por cliente",
                                    value = "[{\"receiptId\":1,\"transactionId\":101,\"orderId\":201,\"customerRut\":\"12345678-9\",\"issueDate\":\"2025-07-18T14:00:00\",\"totalAmount\":15000.0,\"currency\":\"CLP\"}, {\"receiptId\":3,\"transactionId\":103,\"orderId\":203,\"customerRut\":\"12345678-9\",\"issueDate\":\"2025-07-18T15:00:00\",\"totalAmount\":5000.0,\"currency\":\"CLP\"}]"
                            ))),
            @ApiResponse(responseCode = "400", description = "RUT inválido (ej. formato incorrecto).", content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron recibos para el RUT proporcionado.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @GetMapping("/by-customer/{rut}")
    public ResponseEntity<List<ReceiptResponseDTO>> getByCustomer(
            @Parameter(description = "RUT (Rol Único Tributario) del cliente para buscar recibos. Formato: XXXXXXXX-X.", example = "12345678-9")
            @PathVariable String rut) {
        return ResponseEntity.ok(receiptService.getByCustomerRut(rut));
    }
}
