package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.InvoiceRequestDTO;
import com.ecomarket.backend.payment.DTO.InvoiceResponseDTO;
import com.ecomarket.backend.payment.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@RequestBody InvoiceRequestDTO request) {
        return ResponseEntity.ok(invoiceService.createInvoice(request));
    }

    @GetMapping("/by-transaction/{transactionId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getByTransaction(@PathVariable Long transactionId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByTransactionId(transactionId));
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByOrderId(orderId));
    }
}
