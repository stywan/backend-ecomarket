package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.TransactionRequestDTO;
import com.ecomarket.backend.payment.DTO.TransactionResponseDTO;
import com.ecomarket.backend.payment.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transacciones", description = "Operaciones relacionadas con las transacciones de pago")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Crear una nueva transacción", description = "Registra una nueva transacción en el sistema")
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @RequestBody TransactionRequestDTO request) {
        return ResponseEntity.ok(transactionService.createTransaction(request));
    }

    @Operation(
        summary = "Obtener transacciones",
        description = "Obtiene transacciones filtradas por ID de usuario y/o estado. Al menos un parámetro debe ser proporcionado."
    )
    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions(
            @Parameter(description = "ID del usuario") @RequestParam(required = false) Long userId,
            @Parameter(description = "Estado de la transacción") @RequestParam(required = false) String status
    ) {
        if (userId != null && status != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByUserAndStatus(userId, status));
        } else if (userId != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByUser(userId));
        } else if (status != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByStatus(status));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    @PatchMapping("/{transactionId}/status")
public ResponseEntity<TransactionResponseDTO> updateStatus(
        @PathVariable Long transactionId,
        @RequestParam String newStatus) {
    return ResponseEntity.ok(transactionService.updateTransactionStatus(transactionId, newStatus));
}



}
