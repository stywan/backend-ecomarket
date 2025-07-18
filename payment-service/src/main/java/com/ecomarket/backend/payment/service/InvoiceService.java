package com.ecomarket.backend.payment.service;

import com.ecomarket.backend.payment.DTO.request.InvoiceRequestDTO;
import com.ecomarket.backend.payment.DTO.response.InvoiceResponseDTO;
import com.ecomarket.backend.payment.model.Invoice;
import com.ecomarket.backend.payment.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO request) {
        Invoice invoice = Invoice.builder()
                .orderId(request.getOrderId())
                .transactionId(request.getTransactionId())
                .documentNumber(request.getDocumentNumber())
                .issueDate(LocalDateTime.now())
                .totalAmount(request.getTotalAmount())
                .taxes(request.getTaxes())
                .taxProfileId(request.getTaxProfileId())
                .status(Invoice.Status.ISSUED)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        return convertToDTO(saved);
    }

    public List<InvoiceResponseDTO> getInvoicesByTransactionId(Long transactionId) {
        List<Invoice> invoices = invoiceRepository.findByTransactionId(transactionId);
        if (invoices.isEmpty()) {
            throw new EntityNotFoundException("No invoices found for transaction ID: " + transactionId);
        }
        return invoices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<InvoiceResponseDTO> getInvoicesByOrderId(Long orderId) {
        List<Invoice> invoices = invoiceRepository.findByOrderId(orderId);
        if (invoices.isEmpty()) {
            throw new EntityNotFoundException("No invoices found for order ID: " + orderId);
        }
        return invoices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private InvoiceResponseDTO convertToDTO(Invoice invoice) {
        return InvoiceResponseDTO.builder()
                .invoiceId(invoice.getInvoiceId())
                .orderId(invoice.getOrderId())
                .transactionId(invoice.getTransactionId())
                .documentNumber(invoice.getDocumentNumber())
                .issueDate(invoice.getIssueDate())
                .totalAmount(invoice.getTotalAmount())
                .taxes(invoice.getTaxes())
                .taxProfileId(invoice.getTaxProfileId())
                .status(invoice.getStatus().name())
                .build();
    }
}
