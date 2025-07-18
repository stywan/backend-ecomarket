package com.ecomarket.backend.payment.controller;

import com.ecomarket.backend.payment.DTO.request.ReceiptRequestDTO;
import com.ecomarket.backend.payment.DTO.response.ReceiptResponseDTO;
import com.ecomarket.backend.payment.service.ReceiptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException; // Importar
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceiptController.class)
@DisplayName("ReceiptController Unit Tests")
class ReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReceiptService receiptService;

    // Datos de prueba comunes
    private ReceiptRequestDTO receiptRequestDTO;
    private ReceiptResponseDTO receiptResponseDTO;
    private ReceiptResponseDTO anotherReceiptResponseDTO;

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

        receiptResponseDTO = ReceiptResponseDTO.builder()
                .receiptId(10L)
                .orderId(2L)
                .transactionId(202L)
                .documentNumber("REC-002")
                .issueDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("250.00"))
                .taxes(new BigDecimal("47.50"))
                .customerRut("12345678-9")
                .customerName("Jane Doe")
                .status("ISSUED")
                .build();

        anotherReceiptResponseDTO = ReceiptResponseDTO.builder()
                .receiptId(11L)
                .orderId(3L)
                .transactionId(203L)
                .documentNumber("REC-003")
                .issueDate(LocalDateTime.now().minusDays(1))
                .totalAmount(new BigDecimal("100.00"))
                .taxes(new BigDecimal("19.00"))
                .customerRut("98765432-1")
                .customerName("John Smith")
                .status("ISSUED")
                .build();
    }

    // --- Tests para POST /api/v1/receipts (createReceipt) ---
    @Test
    @DisplayName("POST /receipts - Should create a receipt successfully")
    void createReceipt_success() throws Exception {
        when(receiptService.createReceipt(any(ReceiptRequestDTO.class))).thenReturn(receiptResponseDTO);

        mockMvc.perform(post("/api/v1/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receiptRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptId").value(receiptResponseDTO.getReceiptId()))
                .andExpect(jsonPath("$.transactionId").value(receiptResponseDTO.getTransactionId()))
                .andExpect(jsonPath("$.status").value("ISSUED"));

        verify(receiptService, times(1)).createReceipt(any(ReceiptRequestDTO.class));
    }

    @Test
    @DisplayName("POST /receipts - Should return 400 Bad Request for invalid input (e.g., null totalAmount)")
    void createReceipt_invalidInput() throws Exception {
        // Simulamos un DTO inválido para la validación (ej. totalAmount nulo)
        ReceiptRequestDTO invalidRequestDTO = ReceiptRequestDTO.builder()
                .orderId(2L)
                .transactionId(202L)
                .documentNumber("REC-002")
                // .totalAmount(null) // Esto causaría la validación fallida
                .taxes(new BigDecimal("47.50"))
                .customerRut("12345678-9")
                .customerName("Jane Doe")
                .build();

        mockMvc.perform(post("/api/v1/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());

        verify(receiptService, never()).createReceipt(any(ReceiptRequestDTO.class));
    }

    @Test
    @DisplayName("POST /receipts - Should return 400 Bad Request for invalid RUT format")
    void createReceipt_invalidRutFormat() throws Exception {
        // Simulamos un DTO con RUT en formato inválido
        ReceiptRequestDTO invalidRutDTO = ReceiptRequestDTO.builder()
                .orderId(2L)
                .transactionId(202L)
                .documentNumber("REC-002")
                .totalAmount(new BigDecimal("100.00"))
                .taxes(new BigDecimal("19.00"))
                .customerRut("INVALID_RUT") // Formato inválido
                .customerName("Jane Doe")
                .build();

        mockMvc.perform(post("/api/v1/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRutDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists()); // Mensaje de validación sobre el patrón del RUT

        verify(receiptService, never()).createReceipt(any(ReceiptRequestDTO.class));
    }

    // --- Tests para GET /api/v1/receipts/by-transaction/{transactionId} ---
    @Test
    @DisplayName("GET /by-transaction/{id} - Should return receipts by transaction ID when found")
    void getByTransactionId_success() throws Exception {
        when(receiptService.getByTransactionId(202L)).thenReturn(Arrays.asList(receiptResponseDTO));

        mockMvc.perform(get("/api/v1/receipts/by-transaction/{transactionId}", 202L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiptId").value(receiptResponseDTO.getReceiptId()));

        verify(receiptService, times(1)).getByTransactionId(202L);
    }

    @Test
    @DisplayName("GET /by-transaction/{id} - Should return 404 Not Found when no receipts found by transaction ID")
    void getByTransactionId_notFound() throws Exception {
        // El servicio lanza EntityNotFoundException si no hay recibos
        doThrow(new EntityNotFoundException("No receipts found for transaction ID: 999"))
                .when(receiptService).getByTransactionId(anyLong());

        mockMvc.perform(get("/api/v1/receipts/by-transaction/{transactionId}", 999L))
                .andExpect(status().isNotFound()) // Espera un 404
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No receipts found for transaction ID: 999"));

        verify(receiptService, times(1)).getByTransactionId(999L);
    }

    // --- Tests para GET /api/v1/receipts/by-order/{orderId} ---
    @Test
    @DisplayName("GET /by-order/{id} - Should return receipts by order ID when found")
    void getByOrderId_success() throws Exception {
        when(receiptService.getByOrderId(2L)).thenReturn(Arrays.asList(receiptResponseDTO));

        mockMvc.perform(get("/api/v1/receipts/by-order/{orderId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiptId").value(receiptResponseDTO.getReceiptId()));

        verify(receiptService, times(1)).getByOrderId(2L);
    }

    @Test
    @DisplayName("GET /by-order/{id} - Should return 404 Not Found when no receipts found by order ID")
    void getByOrderId_notFound() throws Exception {
        doThrow(new EntityNotFoundException("No receipts found for order ID: 888"))
                .when(receiptService).getByOrderId(anyLong());

        mockMvc.perform(get("/api/v1/receipts/by-order/{orderId}", 888L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No receipts found for order ID: 888"));

        verify(receiptService, times(1)).getByOrderId(888L);
    }

    // --- Tests para GET /api/v1/receipts/by-customer/{rut} ---
    @Test
    @DisplayName("GET /by-customer/{rut} - Should return receipts by customer RUT when found")
    void getByCustomerRut_success() throws Exception {
        when(receiptService.getByCustomerRut("12345678-9")).thenReturn(Arrays.asList(receiptResponseDTO, anotherReceiptResponseDTO));

        mockMvc.perform(get("/api/v1/receipts/by-customer/{rut}", "12345678-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiptId").value(receiptResponseDTO.getReceiptId()))
                .andExpect(jsonPath("$[1].receiptId").value(anotherReceiptResponseDTO.getReceiptId()))
                .andExpect(jsonPath("$.length()").value(2)); // Verifica que devuelve ambos recibos

        verify(receiptService, times(1)).getByCustomerRut("12345678-9");
    }

    @Test
    @DisplayName("GET /by-customer/{rut} - Should return 404 Not Found when no receipts found by customer RUT")
    void getByCustomerRut_notFound() throws Exception {
        doThrow(new EntityNotFoundException("No receipts found for customer RUT: 00000000-0"))
                .when(receiptService).getByCustomerRut(anyString());

        mockMvc.perform(get("/api/v1/receipts/by-customer/{rut}", "00000000-0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No receipts found for customer RUT: 00000000-0"));

        verify(receiptService, times(1)).getByCustomerRut("00000000-0");
    }

    @Test
    @DisplayName("GET /by-customer/{rut} - Should return 400 Bad Request for invalid RUT format (if service had extra validation)")
    void getByCustomerRut_invalidRutFormat_serviceLevel() throws Exception {
        // Esto simularía que el servicio tiene una validación de RUT más allá del @Pattern del DTO
        doThrow(new IllegalArgumentException("Invalid Chilean RUT format or checksum for: INVALID_RUT_FORMAT"))
                .when(receiptService).getByCustomerRut(anyString());

        mockMvc.perform(get("/api/v1/receipts/by-customer/{rut}", "INVALID_RUT_FORMAT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid Chilean RUT format or checksum for: INVALID_RUT_FORMAT"));

        verify(receiptService, times(1)).getByCustomerRut("INVALID_RUT_FORMAT");
    }
}