package com.ecomarket.backend.payment.service;

import com.ecomarket.backend.payment.DTO.request.ReceiptRequestDTO;
import com.ecomarket.backend.payment.DTO.response.ReceiptResponseDTO;
import com.ecomarket.backend.payment.model.Receipt;
import com.ecomarket.backend.payment.repository.ReceiptRepository;
import jakarta.persistence.EntityNotFoundException; // Importar
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
@DisplayName("ReceiptService Unit Tests")
class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @InjectMocks
    private ReceiptService receiptService;

    // Datos de prueba comunes
    private ReceiptRequestDTO receiptRequestDTO;
    private Receipt receipt;
    private ReceiptResponseDTO receiptResponseDTO;

    @BeforeEach
    void setUp() {
        receiptRequestDTO = ReceiptRequestDTO.builder()
                .orderId(2L)
                .transactionId(202L)
                .documentNumber("REC-002")
                .totalAmount(new BigDecimal("250.00"))
                .taxes(new BigDecimal("47.50"))
                .customerRut("12345678-9")
                .customerName("Jane Doe")
                .build();

        receipt = Receipt.builder()
                .receiptId(10L)
                .orderId(2L)
                .transactionId(202L)
                .documentNumber("REC-002")
                .issueDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("250.00"))
                .taxes(new BigDecimal("47.50"))
                .customerRut("12345678-9")
                .customerName("Jane Doe")
                .status(Receipt.Status.ISSUED)
                .build();

        receiptResponseDTO = ReceiptResponseDTO.builder()
                .receiptId(10L)
                .orderId(2L)
                .transactionId(202L)
                .documentNumber("REC-002")
                .issueDate(receipt.getIssueDate())
                .totalAmount(new BigDecimal("250.00"))
                .taxes(new BigDecimal("47.50"))
                .customerRut("12345678-9")
                .customerName("Jane Doe")
                .status("ISSUED")
                .build();
    }

    @Test
    @DisplayName("Should create a receipt successfully")
    void shouldCreateReceiptSuccessfully() {
        when(receiptRepository.save(any(Receipt.class))).thenReturn(receipt);

        ReceiptResponseDTO result = receiptService.createReceipt(receiptRequestDTO);

        assertNotNull(result);
        assertNotNull(result.getReceiptId());
        assertEquals(receiptResponseDTO.getTransactionId(), result.getTransactionId());
        assertEquals(receiptResponseDTO.getDocumentNumber(), result.getDocumentNumber());
        assertEquals("ISSUED", result.getStatus());
        verify(receiptRepository, times(1)).save(any(Receipt.class));
    }

    @Test
    @DisplayName("Should return receipts by transaction ID when found")
    void shouldReturnReceiptsByTransactionIdWhenFound() {
        when(receiptRepository.findByTransactionId(202L)).thenReturn(Collections.singletonList(receipt));

        List<ReceiptResponseDTO> result = receiptService.getByTransactionId(202L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(receiptResponseDTO.getReceiptId(), result.get(0).getReceiptId());
        verify(receiptRepository, times(1)).findByTransactionId(202L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no receipts found by transaction ID")
    void shouldThrowEntityNotFoundExceptionWhenNoReceiptsFoundByTransactionId() {
        when(receiptRepository.findByTransactionId(999L)).thenReturn(Collections.emptyList());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            receiptService.getByTransactionId(999L);
        });

        assertTrue(thrown.getMessage().contains("No receipts found for transaction ID: 999"));
        verify(receiptRepository, times(1)).findByTransactionId(999L);
    }

    @Test
    @DisplayName("Should return receipts by order ID when found")
    void shouldReturnReceiptsByOrderIdWhenFound() {
        when(receiptRepository.findByOrderId(2L)).thenReturn(Collections.singletonList(receipt));

        List<ReceiptResponseDTO> result = receiptService.getByOrderId(2L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(receiptResponseDTO.getReceiptId(), result.get(0).getReceiptId());
        verify(receiptRepository, times(1)).findByOrderId(2L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no receipts found by order ID")
    void shouldThrowEntityNotFoundExceptionWhenNoReceiptsFoundByOrderId() {
        when(receiptRepository.findByOrderId(888L)).thenReturn(Collections.emptyList());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            receiptService.getByOrderId(888L);
        });

        assertTrue(thrown.getMessage().contains("No receipts found for order ID: 888"));
        verify(receiptRepository, times(1)).findByOrderId(888L);
    }

    @Test
    @DisplayName("Should return receipts by customer RUT when found")
    void shouldReturnReceiptsByCustomerRutWhenFound() {
        when(receiptRepository.findByCustomerRut("12345678-9")).thenReturn(Collections.singletonList(receipt));

        List<ReceiptResponseDTO> result = receiptService.getByCustomerRut("12345678-9");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(receiptResponseDTO.getReceiptId(), result.get(0).getReceiptId());
        verify(receiptRepository, times(1)).findByCustomerRut("12345678-9");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no receipts found by customer RUT")
    void shouldThrowEntityNotFoundExceptionWhenNoReceiptsFoundByCustomerRut() {
        when(receiptRepository.findByCustomerRut("00000000-0")).thenReturn(Collections.emptyList());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            receiptService.getByCustomerRut("00000000-0");
        });

        assertTrue(thrown.getMessage().contains("No receipts found for customer RUT: 00000000-0"));
        verify(receiptRepository, times(1)).findByCustomerRut("00000000-0");
    }

    @Test
    @DisplayName("Should return raw receipts by transaction ID for internal use")
    void shouldReturnRawReceiptsByTransactionId() {
        when(receiptRepository.findByTransactionId(202L)).thenReturn(Collections.singletonList(receipt));

        List<Receipt> result = receiptService.getReceiptsByTransactionId(202L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(receipt.getReceiptId(), result.get(0).getReceiptId());
        verify(receiptRepository, times(1)).findByTransactionId(202L);
    }

    @Test
    @DisplayName("Should return 0 raw receipts by transaction ID if none found")
    void shouldReturn0RawReceiptsIfNoneFound() {
        when(receiptRepository.findByTransactionId(999L)).thenReturn(Collections.emptyList());

        List<Receipt> result = receiptService.getReceiptsByTransactionId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(receiptRepository, times(1)).findByTransactionId(999L);
    }


    @Test
    @DisplayName("Should return the correct count of all receipts")
    void shouldReturnCorrectCountOfAllReceipts() {
        when(receiptRepository.count()).thenReturn(5L);

        long count = receiptService.countAllReceipts();

        assertEquals(5L, count);
        verify(receiptRepository, times(1)).count();
    }
}
