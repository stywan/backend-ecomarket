package com.ecomarket.backend.payment.repository;

import com.ecomarket.backend.payment.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findByTransactionId(Long transactionId);
    List<Receipt> findByOrderId(Long orderId);
    List<Receipt> findByCustomerRut(String customerRut);
}
