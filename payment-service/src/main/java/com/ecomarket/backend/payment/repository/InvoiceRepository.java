package com.ecomarket.backend.payment.repository;

import com.ecomarket.backend.payment.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByTransactionId(Long transactionId);
    List<Invoice> findByOrderId(Long orderId);
    List<Invoice> findByTaxProfileId(Long taxProfileId);
}
