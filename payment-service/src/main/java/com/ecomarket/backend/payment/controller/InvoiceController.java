package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.request.InvoiceRequestDTO;
import com.ecomarket.backend.payment.DTO.response.InvoiceResponseDTO;
import com.ecomarket.backend.payment.service.InvoiceService;
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
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoices", description = "Operaciones para la gestión de facturas")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Operation(summary = "Crear una nueva factura",
            description = "Registra una nueva factura basada en los detalles de una transacción o una orden.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura creada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InvoiceResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de factura creada",
                                    value = "{\"invoiceId\":1,\"transactionId\":101,\"orderId\":201,\"invoiceDate\":\"2025-07-18T10:00:00\",\"totalAmount\":15000.0,\"currency\":\"CLP\"}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes o incorrectos).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Validation error: transactionId must not be null\"}"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceRequestDTO request) {
        return ResponseEntity.ok(invoiceService.createInvoice(request));
    }

    @Operation(summary = "Obtener facturas por ID de transacción",
            description = "Recupera todas las facturas asociadas a un ID de transacción específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de facturas encontrada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InvoiceResponseDTO.class, type = "array"),
                            examples = @ExampleObject(
                                    name = "Ejemplo de lista de facturas por transacción",
                                    value = "[{\"invoiceId\":1,\"transactionId\":101,\"orderId\":201,\"invoiceDate\":\"2025-07-18T10:00:00\",\"totalAmount\":15000.0,\"currency\":\"CLP\"},{\"invoiceId\":2,\"transactionId\":101,\"orderId\":202,\"invoiceDate\":\"2025-07-18T11:00:00\",\"totalAmount\":25000.0,\"currency\":\"CLP\"}]"
                            ))),
            @ApiResponse(responseCode = "404", description = "No se encontraron facturas para el ID de transacción proporcionado.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @GetMapping("/by-transaction/{transactionId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getByTransaction(
            @Parameter(description = "ID de la transacción para buscar facturas.", example = "101")
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByTransactionId(transactionId));
    }

    @Operation(summary = "Obtener facturas por ID de orden",
            description = "Recupera todas las facturas asociadas a un ID de orden de compra específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de facturas encontrada exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InvoiceResponseDTO.class, type = "array"),
                            examples = @ExampleObject(
                                    name = "Ejemplo de lista de facturas por orden",
                                    value = "[{\"invoiceId\":1,\"transactionId\":101,\"orderId\":201,\"invoiceDate\":\"2025-07-18T10:00:00\",\"totalAmount\":15000.0,\"currency\":\"CLP\"}]"
                            ))),
            @ApiResponse(responseCode = "404", description = "No se encontraron facturas para el ID de orden proporcionado.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.", content = @Content)
    })
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getByOrder(
            @Parameter(description = "ID de la orden de compra para buscar facturas.", example = "201")
            @PathVariable Long orderId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByOrderId(orderId));
    }
}