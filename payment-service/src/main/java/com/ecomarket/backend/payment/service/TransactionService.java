package com.ecomarket.backend.payment.service;

import com.ecomarket.backend.payment.DTO.ReceiptRequestDTO;
import com.ecomarket.backend.payment.DTO.TransactionRequestDTO;
import com.ecomarket.backend.payment.DTO.TransactionResponseDTO;
import com.ecomarket.backend.payment.model.Receipt;
import com.ecomarket.backend.payment.model.Transaction;
import com.ecomarket.backend.payment.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ReceiptService receiptService;

    public TransactionService(TransactionRepository transactionRepository, ReceiptService receiptService) {
        this.transactionRepository = transactionRepository;
        this.receiptService = receiptService;
    }

    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request) {
        Transaction transaction = Transaction.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .transactionDate(LocalDateTime.now())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .transactionStatus(Transaction.TransactionStatus.PENDING)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return convertToDTO(saved);
    }

    public List<TransactionResponseDTO> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionResponseDTO> getTransactionsByStatus(String status) {
        Transaction.TransactionStatus statusEnum = Transaction.TransactionStatus.valueOf(status.toUpperCase());
        return transactionRepository.findByTransactionStatus(statusEnum).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionResponseDTO> getTransactionsByUserAndStatus(Long userId, String status) {
        Transaction.TransactionStatus statusEnum = Transaction.TransactionStatus.valueOf(status.toUpperCase());
        return transactionRepository.findByUserIdAndTransactionStatus(userId, statusEnum).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponseDTO updateTransactionStatus(Long transactionId, String newStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setTransactionStatus(Transaction.TransactionStatus.valueOf(newStatus.toUpperCase()));
        transactionRepository.save(transaction);

        // Si cambia a APPROVED, generamos la boleta automáticamente
        if (transaction.getTransactionStatus() == Transaction.TransactionStatus.APPROVED) {
            generateReceiptForTransaction(transaction);
        }

        return convertToDTO(transaction);
    }

    private void generateReceiptForTransaction(Transaction transaction) {
        // Evita duplicados
        List<Receipt> existingReceipts = receiptService.getReceiptsByTransactionId(transaction.getTransactionId());
        if (!existingReceipts.isEmpty()) return;

        // Crear número correlativo de documento
        String documentNumber = "B-2025-" + String.format("%04d", receiptService.countAllReceipts() + 1);

        // Crear boleta a partir de la transacción
        ReceiptRequestDTO receiptRequestDTO = ReceiptRequestDTO.builder()
                .orderId(transaction.getOrderId())
                .transactionId(transaction.getTransactionId())
                .documentNumber(documentNumber)
                .totalAmount(transaction.getAmount())
                .taxes(transaction.getAmount().multiply(new BigDecimal("0.19"))) // IVA 19%
                .customerRut("12345678-9") // temporal, se puede mejorar con auth-service
                .customerName("Nombre del Cliente") // temporal, se puede mejorar con auth-service
                .build();

        receiptService.createReceipt(receiptRequestDTO);
    }

    private TransactionResponseDTO convertToDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .orderId(transaction.getOrderId())
                .userId(transaction.getUserId())
                .transactionDate(transaction.getTransactionDate())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .paymentMethod(transaction.getPaymentMethod())
                .transactionStatus(transaction.getTransactionStatus().name())
                .build();
    }
}
