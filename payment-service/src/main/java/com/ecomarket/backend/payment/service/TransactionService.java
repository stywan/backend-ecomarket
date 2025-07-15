package com.ecomarket.backend.payment.service;

import com.ecomarket.backend.payment.DTO.TransactionRequestDTO;
import com.ecomarket.backend.payment.DTO.TransactionResponseDTO;
import com.ecomarket.backend.payment.model.Transaction;
import com.ecomarket.backend.payment.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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
