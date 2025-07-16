package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.ReceiptRequestDTO;
import com.ecomarket.backend.payment.DTO.ReceiptResponseDTO;
import com.ecomarket.backend.payment.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/receipts")
@Tag(name = "Boletas", description = "Operaciones para gestionar boletas")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @Operation(summary = "Crear una nueva boleta", description = "Registra una nueva boleta en el sistema")
    @PostMapping
    public ResponseEntity<ReceiptResponseDTO> createReceipt(
            @RequestBody ReceiptRequestDTO request) {
        return ResponseEntity.ok(receiptService.createReceipt(request));
    }

    @Operation(summary = "Obtener boletas por transacción", description = "Lista de boletas asociadas a una transacción")
    @GetMapping("/by-transaction/{transactionId}")
    public ResponseEntity<List<ReceiptResponseDTO>> getByTransaction(
            @Parameter(description = "ID de la transacción") @PathVariable Long transactionId) {
        return ResponseEntity.ok(receiptService.getByTransactionId(transactionId));
    }

    @Operation(summary = "Obtener boletas por orden", description = "Lista de boletas asociadas a una orden")
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<ReceiptResponseDTO>> getByOrder(
            @Parameter(description = "ID de la orden") @PathVariable Long orderId) {
        return ResponseEntity.ok(receiptService.getByOrderId(orderId));
    }

    @Operation(summary = "Obtener boletas por cliente", description = "Lista de boletas asociadas a un cliente según su RUT")
    @GetMapping("/by-customer/{rut}")
    public ResponseEntity<List<ReceiptResponseDTO>> getByCustomer(
            @Parameter(description = "RUT del cliente") @PathVariable String rut) {
        return ResponseEntity.ok(receiptService.getByCustomerRut(rut));
    }
}
