package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.ReceiptRequestDTO;
import com.ecomarket.backend.payment.DTO.ReceiptResponseDTO;
import com.ecomarket.backend.payment.service.ReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping
    public ResponseEntity<ReceiptResponseDTO> createReceipt(@RequestBody ReceiptRequestDTO request) {
        return ResponseEntity.ok(receiptService.createReceipt(request));
    }

    @GetMapping("/by-transaction/{transactionId}")
    public ResponseEntity<List<ReceiptResponseDTO>> getByTransaction(@PathVariable Long transactionId) {
        return ResponseEntity.ok(receiptService.getByTransactionId(transactionId));
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<ReceiptResponseDTO>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(receiptService.getByOrderId(orderId));
    }

    @GetMapping("/by-customer/{rut}")
    public ResponseEntity<List<ReceiptResponseDTO>> getByCustomer(@PathVariable String rut) {
        return ResponseEntity.ok(receiptService.getByCustomerRut(rut));
    }
}
