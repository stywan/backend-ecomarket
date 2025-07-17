package com.ecomarket.backend.payment.repository;

import com.ecomarket.backend.payment.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByTransactionStatus(Transaction.TransactionStatus status);
    List<Transaction> findByUserIdAndTransactionStatus(Long userId, Transaction.TransactionStatus status);
}
