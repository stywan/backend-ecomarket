package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.request.TransactionRequestDTO;
import com.ecomarket.backend.payment.DTO.response.TransactionResponseDTO;
import com.ecomarket.backend.payment.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@DisplayName("TransactionController Unit Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    private TransactionRequestDTO transactionRequestDTO;
    private TransactionResponseDTO transactionResponseDTO;

    @BeforeEach
    void setUp() {
        transactionRequestDTO = TransactionRequestDTO.builder()
                .orderId(1L)
                .userId(10L)
                .amount(new BigDecimal("50000.00"))
                .currency("CLP")
                .paymentMethod("Credit Card")
                .build();

        transactionResponseDTO = TransactionResponseDTO.builder()
                .transactionId(1L)
                .orderId(1L)
                .userId(10L)
                .transactionDate(LocalDateTime.now())
                .amount(new BigDecimal("50000.00"))
                .currency("CLP")
                .paymentMethod("Credit Card")
                .transactionStatus("PENDING")
                .build();

        TransactionResponseDTO anotherTransactionResponseDTO = TransactionResponseDTO.builder()
                .transactionId(2L)
                .orderId(2L)
                .userId(11L)
                .transactionDate(LocalDateTime.now().minusDays(1))
                .amount(new BigDecimal("25000.00"))
                .currency("CLP")
                .paymentMethod("Debit Card")
                .transactionStatus("COMPLETED")
                .build();
    }

    @Test
    @DisplayName("POST /transactions - Should create a transaction successfully")
    void createTransaction_success() throws Exception {
        when(transactionService.createTransaction(any(TransactionRequestDTO.class))).thenReturn(transactionResponseDTO);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionResponseDTO.getTransactionId()))
                .andExpect(jsonPath("$.userId").value(transactionResponseDTO.getUserId()))
                .andExpect(jsonPath("$.transactionStatus").value("PENDING"));

        verify(transactionService, times(1)).createTransaction(any(TransactionRequestDTO.class));
    }

    @Test
    @DisplayName("POST /transactions - Should return 400 Bad Request for invalid input")
    void createTransaction_invalidInput() throws Exception {
        TransactionRequestDTO invalidRequestDTO = TransactionRequestDTO.builder()
                .orderId(1L)
                .amount(new BigDecimal("50000.00"))
                .currency("CLP")
                .paymentMethod("Credit Card")
                .build();


        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());

        verify(transactionService, never()).createTransaction(any(TransactionRequestDTO.class));
    }


    @Test
    @DisplayName("GET /transactions - Should return transactions by user ID")
    void getTransactions_byUserId() throws Exception {
        when(transactionService.getTransactionsByUser(10L)).thenReturn(Collections.singletonList(transactionResponseDTO));

        mockMvc.perform(get("/api/v1/transactions")
                        .param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value(transactionResponseDTO.getTransactionId()));

        verify(transactionService, times(1)).getTransactionsByUser(10L);
        verify(transactionService, never()).getTransactionsByStatus(anyString());
        verify(transactionService, never()).getTransactionsByUserAndStatus(anyLong(), anyString());
    }

    @Test
    @DisplayName("GET /transactions - Should return transactions by status")
    void getTransactions_byStatus() throws Exception {
        when(transactionService.getTransactionsByStatus("PENDING")).thenReturn(Collections.singletonList(transactionResponseDTO));

        mockMvc.perform(get("/api/v1/transactions")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value(transactionResponseDTO.getTransactionId()));

        verify(transactionService, times(1)).getTransactionsByStatus("PENDING");
        verify(transactionService, never()).getTransactionsByUser(anyLong());
        verify(transactionService, never()).getTransactionsByUserAndStatus(anyLong(), anyString());
    }

    @Test
    @DisplayName("GET /transactions - Should return transactions by user ID and status")
    void getTransactions_byUserIdAndStatus() throws Exception {
        when(transactionService.getTransactionsByUserAndStatus(10L, "PENDING")).thenReturn(Arrays.asList(transactionResponseDTO));

        mockMvc.perform(get("/api/v1/transactions")
                        .param("userId", "10")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value(transactionResponseDTO.getTransactionId()));

        verify(transactionService, times(1)).getTransactionsByUserAndStatus(10L, "PENDING");
        verify(transactionService, never()).getTransactionsByUser(anyLong());
        verify(transactionService, never()).getTransactionsByStatus(anyString());
    }

    @Test
    @DisplayName("GET /transactions - Should return 200 OK with empty list when no filters match")
    void getTransactions_noMatch() throws Exception {
        when(transactionService.getTransactionsByUser(anyLong())).thenReturn(Collections.emptyList());
        when(transactionService.getTransactionsByStatus(anyString())).thenReturn(Collections.emptyList());
        when(transactionService.getTransactionsByUserAndStatus(anyLong(), anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/transactions")
                        .param("userId", "99")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(transactionService, times(1)).getTransactionsByUserAndStatus(99L, "APPROVED");
    }



    @Test
    @DisplayName("GET /transactions - Should return 400 Bad Request for invalid status (service throws IllegalArgumentException)")
    void getTransactions_invalidStatus() throws Exception {
        when(transactionService.getTransactionsByStatus(anyString())).thenThrow(new IllegalArgumentException("No enum constant for status: INVALID"));

        mockMvc.perform(get("/api/v1/transactions")
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("No enum constant for status: INVALID"));

        verify(transactionService, times(1)).getTransactionsByStatus("INVALID");
    }

    @Test
    @DisplayName("PATCH /transactions/{id}/status - Should update transaction status successfully")
    void updateTransactionStatus_success() throws Exception {
        TransactionResponseDTO updatedDTO = TransactionResponseDTO.builder()
                .transactionId(1L)
                .orderId(1L)
                .userId(10L)
                .transactionDate(transactionResponseDTO.getTransactionDate())
                .amount(new BigDecimal("50000.00"))
                .currency("CLP")
                .paymentMethod("Credit Card")
                .transactionStatus("APPROVED")
                .build();

        when(transactionService.updateTransactionStatus(1L, "APPROVED")).thenReturn(updatedDTO);

        mockMvc.perform(patch("/api/v1/transactions/{transactionId}/status", 1L)
                        .param("newStatus", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1L))
                .andExpect(jsonPath("$.transactionStatus").value("APPROVED"));

        verify(transactionService, times(1)).updateTransactionStatus(1L, "APPROVED");
    }

    @Test
    @DisplayName("PATCH /transactions/{id}/status - Should return 400 Bad Request for invalid newStatus")
    void updateTransactionStatus_invalidStatus() throws Exception {
        doThrow(new IllegalArgumentException("No enum constant for status: INVALID_STATE")).when(transactionService).updateTransactionStatus(anyLong(), anyString());

        mockMvc.perform(patch("/api/v1/transactions/{transactionId}/status", 1L)
                        .param("newStatus", "INVALID_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("No enum constant for status: INVALID_STATE"));

        verify(transactionService, times(1)).updateTransactionStatus(1L, "INVALID_STATE");
    }
}
