package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.TransactionRequestDTO;
import com.ecomarket.backend.payment.DTO.TransactionResponseDTO;
import com.ecomarket.backend.payment.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@RequestBody TransactionRequestDTO request) {
        return ResponseEntity.ok(transactionService.createTransaction(request));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status
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
}
