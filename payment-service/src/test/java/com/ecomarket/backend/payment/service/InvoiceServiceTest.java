package com.ecomarket.backend.payment.service;
import com.ecomarket.backend.payment.DTO.request.InvoiceRequestDTO;
import com.ecomarket.backend.payment.DTO.response.InvoiceResponseDTO;
import com.ecomarket.backend.payment.model.Invoice;
import com.ecomarket.backend.payment.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException; // Importa la excepción
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceService Unit Tests")
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private InvoiceRequestDTO invoiceRequestDTO;
    private Invoice invoice;
    private InvoiceResponseDTO invoiceResponseDTO;

    @BeforeEach
    void setUp() {
        invoiceRequestDTO = InvoiceRequestDTO.builder()
                .orderId(1L)
                .transactionId(101L)
                .documentNumber("INV-001")
                .totalAmount(new BigDecimal("100.00"))
                .taxes(new BigDecimal("10.00"))
                .taxProfileId(5L)
                .build();

        invoice = Invoice.builder()
                .invoiceId(1L)
                .orderId(1L)
                .transactionId(101L)
                .documentNumber("INV-001")
                .issueDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("100.00"))
                .taxes(new BigDecimal("10.00"))
                .taxProfileId(5L)
                .status(Invoice.Status.ISSUED)
                .build();

        invoiceResponseDTO = InvoiceResponseDTO.builder()
                .invoiceId(1L)
                .orderId(1L)
                .transactionId(101L)
                .documentNumber("INV-001")
                .issueDate(invoice.getIssueDate())
                .totalAmount(new BigDecimal("100.00"))
                .taxes(new BigDecimal("10.00"))
                .taxProfileId(5L)
                .status("ISSUED")
                .build();
    }

    @Test
    @DisplayName("Should create an invoice successfully")
    void shouldCreateInvoiceSuccessfully() {
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        InvoiceResponseDTO result = invoiceService.createInvoice(invoiceRequestDTO);

        assertNotNull(result);
        assertNotNull(result.getInvoiceId());
        assertEquals(invoiceResponseDTO.getOrderId(), result.getOrderId());
        assertEquals(invoiceResponseDTO.getTransactionId(), result.getTransactionId());
        assertEquals(invoiceResponseDTO.getDocumentNumber(), result.getDocumentNumber());
        assertEquals(invoiceResponseDTO.getTotalAmount(), result.getTotalAmount());
        assertEquals(invoiceResponseDTO.getTaxes(), result.getTaxes());
        assertEquals(invoiceResponseDTO.getTaxProfileId(), result.getTaxProfileId());
        assertEquals(invoiceResponseDTO.getStatus(), result.getStatus());

        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should return invoices by transaction ID when found")
    void shouldReturnInvoicesByTransactionIdWhenFound() {
        when(invoiceRepository.findByTransactionId(101L)).thenReturn(Collections.singletonList(invoice));

        List<InvoiceResponseDTO> result = invoiceService.getInvoicesByTransactionId(101L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(invoiceResponseDTO.getInvoiceId(), result.get(0).getInvoiceId());

        verify(invoiceRepository, times(1)).findByTransactionId(101L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no invoices found by transaction ID")
    void shouldThrowEntityNotFoundExceptionWhenNoInvoicesFoundByTransactionId() {
        when(invoiceRepository.findByTransactionId(999L)).thenReturn(Collections.emptyList());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            invoiceService.getInvoicesByTransactionId(999L);
        });

        assertTrue(thrown.getMessage().contains("No invoices found for transaction ID: 999"));

        verify(invoiceRepository, times(1)).findByTransactionId(999L);
    }

    @Test
    @DisplayName("Should return invoices by order ID when found")
    void shouldReturnInvoicesByOrderIdWhenFound() {
        when(invoiceRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(invoice));

        List<InvoiceResponseDTO> result = invoiceService.getInvoicesByOrderId(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(invoiceResponseDTO.getInvoiceId(), result.get(0).getInvoiceId());

        verify(invoiceRepository, times(1)).findByOrderId(1L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no invoices found by order ID")
    void shouldThrowEntityNotFoundExceptionWhenNoInvoicesFoundByOrderId() {
        when(invoiceRepository.findByOrderId(999L)).thenReturn(Collections.emptyList());
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            invoiceService.getInvoicesByOrderId(999L);
        });
        assertTrue(thrown.getMessage().contains("No invoices found for order ID: 999"));

        verify(invoiceRepository, times(1)).findByOrderId(999L);
    }
}