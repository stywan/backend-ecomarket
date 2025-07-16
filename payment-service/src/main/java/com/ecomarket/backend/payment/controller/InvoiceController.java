package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.InvoiceRequestDTO;
import com.ecomarket.backend.payment.DTO.InvoiceResponseDTO;
import com.ecomarket.backend.payment.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@Tag(name = "Facturas", description = "Operaciones para gestionar facturas")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Operation(summary = "Crear una nueva factura", description = "Registra una nueva factura en el sistema")
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(
            @RequestBody InvoiceRequestDTO request) {
        return ResponseEntity.ok(invoiceService.createInvoice(request));
    }

    @Operation(summary = "Obtener facturas por transacción", description = "Obtiene una lista de facturas asociadas a una transacción específica")
    @GetMapping("/by-transaction/{transactionId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getByTransaction(
            @Parameter(description = "ID de la transacción") @PathVariable Long transactionId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByTransactionId(transactionId));
    }

    @Operation(summary = "Obtener facturas por orden", description = "Obtiene una lista de facturas asociadas a una orden específica")
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getByOrder(
            @Parameter(description = "ID de la orden") @PathVariable Long orderId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByOrderId(orderId));
    }
}
