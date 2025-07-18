package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.request.InvoiceRequestDTO;
import com.ecomarket.backend.payment.DTO.response.InvoiceResponseDTO;
import com.ecomarket.backend.payment.service.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
@DisplayName("InvoiceController Unit Tests")
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvoiceService invoiceService;

    private InvoiceRequestDTO invoiceRequestDTO;
    private InvoiceResponseDTO invoiceResponseDTO;
    private InvoiceResponseDTO anotherInvoiceResponseDTO;

    @BeforeEach
    void setUp() {
        invoiceRequestDTO = InvoiceRequestDTO.builder()
                .orderId(201L)
                .transactionId(101L)
                .documentNumber("INV-2025-001")
                .totalAmount(new BigDecimal("15000.00"))
                .taxes(new BigDecimal("2850.00"))
                .taxProfileId(1L)
                .build();

        invoiceResponseDTO = InvoiceResponseDTO.builder()
                .invoiceId(1L)
                .orderId(201L)
                .transactionId(101L)
                .documentNumber("INV-2025-001")
                .issueDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("15000.00"))
                .taxes(new BigDecimal("2850.00"))
                .taxProfileId(1L)
                .status("ISSUED")
                .build();

        anotherInvoiceResponseDTO = InvoiceResponseDTO.builder()
                .invoiceId(2L)
                .orderId(202L)
                .transactionId(101L)
                .documentNumber("INV-2025-002")
                .issueDate(LocalDateTime.now().plusHours(1))
                .totalAmount(new BigDecimal("25000.00"))
                .taxes(new BigDecimal("4750.00"))
                .taxProfileId(1L)
                .status("ISSUED")
                .build();
    }

    @Test
    @DisplayName("POST /invoices - Should create an invoice successfully")
    void createInvoice_success() throws Exception {
        when(invoiceService.createInvoice(any(InvoiceRequestDTO.class))).thenReturn(invoiceResponseDTO);

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value(invoiceResponseDTO.getInvoiceId()))
                .andExpect(jsonPath("$.transactionId").value(invoiceResponseDTO.getTransactionId()))
                .andExpect(jsonPath("$.status").value("ISSUED"));

        verify(invoiceService, times(1)).createInvoice(any(InvoiceRequestDTO.class));
    }

    @Test
    @DisplayName("POST /invoices - Should return 400 Bad Request for invalid input (e.g., null transactionId)")
    void createInvoice_invalidInput() throws Exception {
        InvoiceRequestDTO invalidRequestDTO = InvoiceRequestDTO.builder()
                .orderId(201L)
                .transactionId(null) // Make it invalid
                .documentNumber("INV-2025-001")
                .totalAmount(new BigDecimal("15000.00"))
                .taxes(new BigDecimal("2850.00"))
                .taxProfileId(1L)
                .build();

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());

        verify(invoiceService, never()).createInvoice(any(InvoiceRequestDTO.class));
    }

    @Test
    @DisplayName("GET /by-transaction/{transactionId} - Should return invoices by transaction ID when found")
    void getByTransactionId_success() throws Exception {
        when(invoiceService.getInvoicesByTransactionId(101L)).thenReturn(Arrays.asList(invoiceResponseDTO, anotherInvoiceResponseDTO));

        mockMvc.perform(get("/api/v1/invoices/by-transaction/{transactionId}", 101L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceId").value(invoiceResponseDTO.getInvoiceId()))
                .andExpect(jsonPath("$[1].invoiceId").value(anotherInvoiceResponseDTO.getInvoiceId()))
                .andExpect(jsonPath("$.length()").value(2));

        verify(invoiceService, times(1)).getInvoicesByTransactionId(101L);
    }

    @Test
    @DisplayName("GET /by-transaction/{transactionId} - Should return 404 Not Found when no invoices found by transaction ID")
    void getByTransactionId_notFound() throws Exception {
        doThrow(new EntityNotFoundException("No invoices found for transaction ID: 999"))
                .when(invoiceService).getInvoicesByTransactionId(anyLong());

        mockMvc.perform(get("/api/v1/invoices/by-transaction/{transactionId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No invoices found for transaction ID: 999"));

        verify(invoiceService, times(1)).getInvoicesByTransactionId(999L);
    }

    @Test
    @DisplayName("GET /by-order/{orderId} - Should return invoices by order ID when found")
    void getByOrderId_success() throws Exception {
        when(invoiceService.getInvoicesByOrderId(201L)).thenReturn(Arrays.asList(invoiceResponseDTO));

        mockMvc.perform(get("/api/v1/invoices/by-order/{orderId}", 201L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceId").value(invoiceResponseDTO.getInvoiceId()));

        verify(invoiceService, times(1)).getInvoicesByOrderId(201L);
    }

    @Test
    @DisplayName("GET /by-order/{orderId} - Should return 404 Not Found when no invoices found by order ID")
    void getByOrderId_notFound() throws Exception {
        doThrow(new EntityNotFoundException("No invoices found for order ID: 888"))
                .when(invoiceService).getInvoicesByOrderId(anyLong());

        mockMvc.perform(get("/api/v1/invoices/by-order/{orderId}", 888L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No invoices found for order ID: 888"));

        verify(invoiceService, times(1)).getInvoicesByOrderId(888L);
    }
}
