package com.ecomarket.backend.payment.service;

import com.ecomarket.backend.payment.DTO.request.ReceiptRequestDTO;
import com.ecomarket.backend.payment.DTO.request.TransactionRequestDTO;
import com.ecomarket.backend.payment.DTO.response.TransactionResponseDTO;
import com.ecomarket.backend.payment.model.Receipt;
import com.ecomarket.backend.payment.model.Transaction;
import com.ecomarket.backend.payment.repository.TransactionRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ReceiptService receiptService;

    @InjectMocks
    private TransactionService transactionService;

    // Datos de prueba comunes
    private TransactionRequestDTO transactionRequestDTO;
    private Transaction transactionPending;
    private Transaction transactionApproved;
    private TransactionResponseDTO transactionResponseDTOPending;

    @BeforeEach
    void setUp() {
        transactionRequestDTO = TransactionRequestDTO.builder()
                .orderId(1L)
                .userId(10L)
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .paymentMethod("Credit Card")
                .build();

        transactionPending = Transaction.builder()
                .transactionId(1L)
                .orderId(1L)
                .userId(10L)
                .transactionDate(LocalDateTime.now())
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .paymentMethod("Credit Card")
                .transactionStatus(Transaction.TransactionStatus.PENDING)
                .build();

        transactionApproved = Transaction.builder()
                .transactionId(1L)
                .orderId(1L)
                .userId(10L)
                .transactionDate(transactionPending.getTransactionDate()) // Mismo timestamp para consistencia
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .paymentMethod("Credit Card")
                .transactionStatus(Transaction.TransactionStatus.APPROVED)
                .build();

        transactionResponseDTOPending = TransactionResponseDTO.builder()
                .transactionId(1L)
                .orderId(1L)
                .userId(10L)
                .transactionDate(transactionPending.getTransactionDate())
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .paymentMethod("Credit Card")
                .transactionStatus("PENDING")
                .build();

        TransactionResponseDTO transactionResponseDTOApproved = TransactionResponseDTO.builder()
                .transactionId(1L)
                .orderId(1L)
                .userId(10L)
                .transactionDate(transactionApproved.getTransactionDate())
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .paymentMethod("Credit Card")
                .transactionStatus("APPROVED")
                .build();
    }

    @Test
    @DisplayName("Should create a transaction successfully")
    void shouldCreateTransactionSuccessfully() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transactionPending);

        TransactionResponseDTO result = transactionService.createTransaction(transactionRequestDTO);

        assertNotNull(result);
        assertNotNull(result.getTransactionId());
        assertEquals(transactionResponseDTOPending.getUserId(), result.getUserId());
        assertEquals("PENDING", result.getTransactionStatus());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should return transactions by user ID when found")
    void shouldReturnTransactionsByUserIdWhenFound() {
        when(transactionRepository.findByUserId(10L)).thenReturn(Collections.singletonList(transactionPending));

        List<TransactionResponseDTO> result = transactionService.getTransactionsByUser(10L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(transactionResponseDTOPending.getTransactionId(), result.get(0).getTransactionId());
        verify(transactionRepository, times(1)).findByUserId(10L);
    }

    @Test
    @DisplayName("Should return empty list when no transactions found by user ID")
    void shouldReturnEmptyListWhenNoTransactionsFoundByUserId() {
        when(transactionRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

        List<TransactionResponseDTO> result = transactionService.getTransactionsByUser(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findByUserId(99L);
    }

    @Test
    @DisplayName("Should return transactions by status when found")
    void shouldReturnTransactionsByStatusWhenFound() {
        when(transactionRepository.findByTransactionStatus(Transaction.TransactionStatus.PENDING))
                .thenReturn(Collections.singletonList(transactionPending));

        List<TransactionResponseDTO> result = transactionService.getTransactionsByStatus("PENDING");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(transactionResponseDTOPending.getTransactionId(), result.get(0).getTransactionId());
        verify(transactionRepository, times(1)).findByTransactionStatus(Transaction.TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("Should return empty list when no transactions found by status")
    void shouldReturnEmptyListWhenNoTransactionsFoundByStatus() {
        when(transactionRepository.findByTransactionStatus(Transaction.TransactionStatus.APPROVED))
                .thenReturn(Collections.emptyList());

        List<TransactionResponseDTO> result = transactionService.getTransactionsByStatus("APPROVED");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findByTransactionStatus(Transaction.TransactionStatus.APPROVED);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when status is invalid for getTransactionsByStatus")
    void shouldThrowIllegalArgumentExceptionWhenStatusIsInvalidForGetTransactionsByStatus() {
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.getTransactionsByStatus("INVALID_STATUS");
        });
        verify(transactionRepository, never()).findByTransactionStatus(any());
    }


    @Test
    @DisplayName("Should return transactions by user ID and status when found")
    void shouldReturnTransactionsByUserIdAndStatusWhenFound() {
        when(transactionRepository.findByUserIdAndTransactionStatus(10L, Transaction.TransactionStatus.PENDING))
                .thenReturn(Collections.singletonList(transactionPending));

        List<TransactionResponseDTO> result = transactionService.getTransactionsByUserAndStatus(10L, "PENDING");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(transactionResponseDTOPending.getTransactionId(), result.get(0).getTransactionId());
        verify(transactionRepository, times(1)).findByUserIdAndTransactionStatus(10L, Transaction.TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("Should return empty list when no transactions found by user ID and status")
    void shouldReturnEmptyListWhenNoTransactionsFoundByUserIdAndStatus() {
        when(transactionRepository.findByUserIdAndTransactionStatus(10L, Transaction.TransactionStatus.APPROVED))
                .thenReturn(Collections.emptyList());

        List<TransactionResponseDTO> result = transactionService.getTransactionsByUserAndStatus(10L, "APPROVED");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findByUserIdAndTransactionStatus(10L, Transaction.TransactionStatus.APPROVED);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when status is invalid for getTransactionsByUserAndStatus")
    void shouldThrowIllegalArgumentExceptionWhenStatusIsInvalidForGetTransactionsByUserAndStatus() {
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.getTransactionsByUserAndStatus(10L, "BAD_STATUS");
        });
        // No hay interacción con el repositorio si el valueOf falla
        verify(transactionRepository, never()).findByUserIdAndTransactionStatus(anyLong(), any());
    }



    @Test
    @DisplayName("Should update transaction status to APPROVED and generate receipt when no existing receipt")
    void shouldUpdateTransactionStatusToApprovedAndGenerateReceipt() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transactionPending));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transactionApproved);
        when(receiptService.getReceiptsByTransactionId(anyLong())).thenReturn(Collections.emptyList());
        when(receiptService.countAllReceipts()).thenReturn(0L);
        when(receiptService.createReceipt(any(ReceiptRequestDTO.class))).thenReturn(null);

        TransactionResponseDTO result = transactionService.updateTransactionStatus(1L, "APPROVED");

        assertNotNull(result);
        assertEquals("APPROVED", result.getTransactionStatus());
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(receiptService, times(1)).getReceiptsByTransactionId(1L);
        verify(receiptService, times(1)).countAllReceipts();
        verify(receiptService, times(1)).createReceipt(any(ReceiptRequestDTO.class));


    }

    @Test
    @DisplayName("Should update transaction status to APPROVED but NOT generate receipt if existing receipt")
    void shouldUpdateTransactionStatusToApprovedButNotGenerateReceiptIfExistingReceipt() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transactionPending));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transactionApproved);
        when(receiptService.getReceiptsByTransactionId(anyLong())).thenReturn(List.of(new Receipt()));

        TransactionResponseDTO result = transactionService.updateTransactionStatus(1L, "APPROVED");

        assertNotNull(result);
        assertEquals("APPROVED", result.getTransactionStatus());
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(receiptService, times(1)).getReceiptsByTransactionId(1L);
        verify(receiptService, never()).countAllReceipts();
        verify(receiptService, never()).createReceipt(any(ReceiptRequestDTO.class));
    }


    @Test
    @DisplayName("Should throw RuntimeException when transaction not found for updateStatus")
    void shouldThrowRuntimeExceptionWhenTransactionNotFoundForUpdateStatus() {
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            transactionService.updateTransactionStatus(99L, "APPROVED");
        });

        assertTrue(thrown.getMessage().contains("Transaction not found"));
        verify(transactionRepository, times(1)).findById(99L);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(receiptService, never()).getReceiptsByTransactionId(anyLong());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when newStatus is invalid for updateStatus")
    void shouldThrowIllegalArgumentExceptionWhenNewStatusIsInvalidForUpdateStatus() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transactionPending));

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.updateTransactionStatus(1L, "INVALID_STATUS");
        });

        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(receiptService, never()).getReceiptsByTransactionId(anyLong());
    }
}
