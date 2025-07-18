package com.ecomarket.backend.payment.service;

import com.ecomarket.backend.payment.DTO.request.ReceiptRequestDTO;
import com.ecomarket.backend.payment.DTO.response.ReceiptResponseDTO;
import com.ecomarket.backend.payment.model.Receipt;
import com.ecomarket.backend.payment.repository.ReceiptRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;

    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    @Transactional
    public ReceiptResponseDTO createReceipt(ReceiptRequestDTO request) {
        Receipt receipt = Receipt.builder()
                .orderId(request.getOrderId())
                .transactionId(request.getTransactionId())
                .documentNumber(request.getDocumentNumber())
                .issueDate(LocalDateTime.now())
                .totalAmount(request.getTotalAmount())
                .taxes(request.getTaxes())
                .customerRut(request.getCustomerRut())
                .customerName(request.getCustomerName())
                .status(Receipt.Status.ISSUED)
                .build();

        return convertToDTO(receiptRepository.save(receipt));
    }

    public List<ReceiptResponseDTO> getByTransactionId(Long transactionId) {
        List<Receipt> receipts = receiptRepository.findByTransactionId(transactionId);
        if (receipts.isEmpty()) {
            throw new EntityNotFoundException("No receipts found for transaction ID: " + transactionId);
        }
        return receipts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReceiptResponseDTO> getByOrderId(Long orderId) {
        List<Receipt> receipts = receiptRepository.findByOrderId(orderId);
        if (receipts.isEmpty()) {
            throw new EntityNotFoundException("No receipts found for order ID: " + orderId);
        }
        return receipts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReceiptResponseDTO> getByCustomerRut(String rut) {

        List<Receipt> receipts = receiptRepository.findByCustomerRut(rut);
        if (receipts.isEmpty()) {
            throw new EntityNotFoundException("No receipts found for customer RUT: " + rut);
        }
        return receipts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ReceiptResponseDTO convertToDTO(Receipt receipt) {
        return ReceiptResponseDTO.builder()
                .receiptId(receipt.getReceiptId())
                .orderId(receipt.getOrderId())
                .transactionId(receipt.getTransactionId())
                .documentNumber(receipt.getDocumentNumber())
                .issueDate(receipt.getIssueDate())
                .totalAmount(receipt.getTotalAmount())
                .taxes(receipt.getTaxes())
                .customerRut(receipt.getCustomerRut())
                .customerName(receipt.getCustomerName())
                .status(receipt.getStatus().name())
                .build();
    }

    public List<Receipt> getReceiptsByTransactionId(Long transactionId) {
        return receiptRepository.findByTransactionId(transactionId);
    }

    public long countAllReceipts() {
        return receiptRepository.count();
    }

}
