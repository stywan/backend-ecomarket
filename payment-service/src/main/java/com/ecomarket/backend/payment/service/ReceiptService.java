package com.ecomarket.backend.payment.service;

import com.ecomarket.backend.payment.DTO.ReceiptRequestDTO;
import com.ecomarket.backend.payment.DTO.ReceiptResponseDTO;
import com.ecomarket.backend.payment.model.Receipt;
import com.ecomarket.backend.payment.repository.ReceiptRepository;
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
        return receiptRepository.findByTransactionId(transactionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReceiptResponseDTO> getByOrderId(Long orderId) {
        return receiptRepository.findByOrderId(orderId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReceiptResponseDTO> getByCustomerRut(String rut) {
        return receiptRepository.findByCustomerRut(rut).stream()
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
}
