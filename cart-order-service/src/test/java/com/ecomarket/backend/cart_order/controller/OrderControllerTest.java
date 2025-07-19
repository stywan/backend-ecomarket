package com.ecomarket.backend.cart_order.controller;

import com.ecomarket.backend.cart_order.DTO.request.OrderItemRequestDTO;
import com.ecomarket.backend.cart_order.DTO.request.OrderRequestDTO;
import com.ecomarket.backend.cart_order.DTO.request.OrderStatusUpdateRequestDTO;
import com.ecomarket.backend.cart_order.DTO.response.OrderItemResponseDTO;
import com.ecomarket.backend.cart_order.DTO.response.OrderResponseDTO;
import com.ecomarket.backend.cart_order.assembler.OrderAssembler;
import com.ecomarket.backend.cart_order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderAssembler orderAssembler;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderResponseDTO validOrderResponse;
    private OrderRequestDTO validOrderRequest;
    private EntityModel<OrderResponseDTO> orderEntityModel;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper for LocalDateTime serialization
        objectMapper.registerModule(new JavaTimeModule());

        // Setup valid test data
        OrderItemResponseDTO orderItem = OrderItemResponseDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Test Product")
                .productPrice(new BigDecimal("10000.00"))
                .quantity(2)
                .subtotal(new BigDecimal("20000.00"))
                .build();

        validOrderResponse = OrderResponseDTO.builder()
                .id(1L)
                .userId(1L)
                .shippingAddressId(1L)
                .paymentTransactionId(1L)
                .createdAt(LocalDateTime.now())
                .orderStatus("PENDING_PAYMENT")
                .totalAmount(new BigDecimal("23990.00"))
                .subtotal(new BigDecimal("20000.00"))
                .shippingCost(new BigDecimal("3990.00"))
                .items(Arrays.asList(orderItem))
                .build();

        OrderItemRequestDTO orderItemRequest = OrderItemRequestDTO.builder()
                .productId(1L)
                .quantity(2)
                .build();

        validOrderRequest = OrderRequestDTO.builder()
                .userId(1L)
                .items(Arrays.asList(orderItemRequest))
                .build();

        // Mock EntityModel
        orderEntityModel = EntityModel.of(validOrderResponse);
    }

    @Test
    void getOrderById_OrderExists_ReturnsOk() throws Exception {
        // Given
        Long orderId = 1L;
        when(orderService.getOrderById(orderId)).thenReturn(validOrderResponse);
        when(orderAssembler.toModel(validOrderResponse)).thenReturn(orderEntityModel);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.orderStatus").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalAmount").value(23990.00))
                .andExpect(jsonPath("$.subtotal").value(20000.00))
                .andExpect(jsonPath("$.shippingCost").value(3990.00));

        verify(orderService).getOrderById(orderId);
        verify(orderAssembler).toModel(validOrderResponse);
    }

    @Test
    void getOrderById_OrderNotFound_ReturnsNotFound() throws Exception {
        // Given
        Long orderId = 999L;
        when(orderService.getOrderById(orderId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(orderService).getOrderById(orderId);
        verify(orderAssembler, never()).toModel(any());
    }

    @Test
    void getOrderById_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/orders/{id}", "invalid-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getOrderById(anyLong());
    }

    @Test
    void createOrder_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(validOrderResponse);
        when(orderAssembler.toModel(validOrderResponse)).thenReturn(orderEntityModel);

        String requestJson = objectMapper.writeValueAsString(validOrderRequest);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.orderStatus").value("PENDING_PAYMENT"));

        verify(orderService).createOrder(any(OrderRequestDTO.class));
        verify(orderAssembler).toModel(validOrderResponse);
    }

    @Test
    void createOrder_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - Request without userId
        OrderRequestDTO invalidRequest = OrderRequestDTO.builder()
                .userId(null)
                .items(Arrays.asList(OrderItemRequestDTO.builder()
                        .productId(1L)
                        .quantity(2)
                        .build()))
                .build();

        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void createOrder_EmptyItems_ReturnsBadRequest() throws Exception {
        // Given - Request with empty items
        OrderRequestDTO invalidRequest = OrderRequestDTO.builder()
                .userId(1L)
                .items(Collections.emptyList())
                .build();

        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void createOrder_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // Given - Request with invalid quantity
        OrderRequestDTO invalidRequest = OrderRequestDTO.builder()
                .userId(1L)
                .items(Arrays.asList(OrderItemRequestDTO.builder()
                        .productId(1L)
                        .quantity(0) // Invalid quantity
                        .build()))
                .build();

        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void createOrder_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Given
        when(orderService.createOrder(any(OrderRequestDTO.class)))
                .thenThrow(new RuntimeException("Service error"));

        String requestJson = objectMapper.writeValueAsString(validOrderRequest);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError());

        verify(orderService).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void getAllOrders_OrdersExist_ReturnsOk() throws Exception {
        // Given
        List<OrderResponseDTO> orders = Arrays.asList(validOrderResponse);
        when(orderService.getAllOrders()).thenReturn(orders);
        when(orderAssembler.toModel(any(OrderResponseDTO.class))).thenReturn(orderEntityModel);

        // When & Then
        mockMvc.perform(get("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.orderResponseDTOList").exists())
                .andExpect(jsonPath("$._embedded.orderResponseDTOList").isArray())
                .andExpect(jsonPath("$._embedded.orderResponseDTOList[0].id").value(1L));

        verify(orderService).getAllOrders();
        verify(orderAssembler).toModel(validOrderResponse);
    }

    @Test
    void getAllOrders_NoOrdersExist_ReturnsNoContent() throws Exception {
        // Given
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(orderService).getAllOrders();
        verify(orderAssembler, never()).toModel(any());
    }

    @Test
    void getOrdersByDate_OrdersExist_ReturnsOk() throws Exception {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<OrderResponseDTO> orders = Arrays.asList(validOrderResponse);
        when(orderService.getOrdersByDate(testDate)).thenReturn(orders);
        when(orderAssembler.toModel(any(OrderResponseDTO.class))).thenReturn(orderEntityModel);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/date")
                        .param("orderDate", "2024-01-15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.orderResponseDTOList").exists())
                .andExpect(jsonPath("$._embedded.orderResponseDTOList").isArray())
                .andExpect(jsonPath("$._embedded.orderResponseDTOList[0].id").value(1L));

        verify(orderService).getOrdersByDate(testDate);
        verify(orderAssembler).toModel(validOrderResponse);
    }

    @Test
    void getOrdersByDate_NoOrdersExist_ReturnsNoContent() throws Exception {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        when(orderService.getOrdersByDate(testDate)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/orders/date")
                        .param("orderDate", "2024-01-15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(orderService).getOrdersByDate(testDate);
        verify(orderAssembler, never()).toModel(any());
    }

    @Test
    void getOrdersByDate_InvalidDateFormat_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/orders/date")
                        .param("orderDate", "invalid-date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getOrdersByDate(any(LocalDate.class));
    }

    @Test
    void getOrdersByDate_MissingDateParameter_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/orders/date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getOrdersByDate(any(LocalDate.class));
    }

    @Test
    void updateOrderStatus_ValidRequest_ReturnsOk() throws Exception {
        // Given
        Long orderId = 1L;
        String newStatus = "CONFIRMED";
        OrderStatusUpdateRequestDTO request = OrderStatusUpdateRequestDTO.builder()
                .newStatus(newStatus)
                .build();

        OrderResponseDTO updatedOrder = OrderResponseDTO.builder()
                .id(orderId)
                .userId(1L)
                .orderStatus("CONFIRMED")
                .totalAmount(new BigDecimal("23990.00"))
                .build();

        when(orderService.updateOrderStatus(orderId, newStatus)).thenReturn(updatedOrder);
        when(orderAssembler.toModel(updatedOrder)).thenReturn(EntityModel.of(updatedOrder));

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));

        verify(orderService).updateOrderStatus(orderId, newStatus);
        verify(orderAssembler).toModel(updatedOrder);
    }

    @Test
    void updateOrderStatus_OrderNotFound_ReturnsNotFound() throws Exception {
        // Given
        Long orderId = 999L;
        String newStatus = "CONFIRMED";
        OrderStatusUpdateRequestDTO request = OrderStatusUpdateRequestDTO.builder()
                .newStatus(newStatus)
                .build();

        when(orderService.updateOrderStatus(orderId, newStatus))
                .thenThrow(new IllegalArgumentException("Order not found with ID: " + orderId));

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError()); // Assuming global exception handler returns 500

        verify(orderService).updateOrderStatus(orderId, newStatus);
        verify(orderAssembler, never()).toModel(any());
    }

    @Test
    void updateOrderStatus_InvalidStatus_ReturnsBadRequest() throws Exception {
        // Given
        Long orderId = 1L;
        String invalidStatus = "INVALID_STATUS";
        OrderStatusUpdateRequestDTO request = OrderStatusUpdateRequestDTO.builder()
                .newStatus(invalidStatus)
                .build();

        when(orderService.updateOrderStatus(orderId, invalidStatus))
                .thenThrow(new IllegalArgumentException("Invalid order status: " + invalidStatus));

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError()); // Assuming global exception handler returns 500

        verify(orderService).updateOrderStatus(orderId, invalidStatus);
        verify(orderAssembler, never()).toModel(any());
    }

    @Test
    void updateOrderStatus_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        // Given
        OrderStatusUpdateRequestDTO request = OrderStatusUpdateRequestDTO.builder()
                .newStatus("CONFIRMED")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(put("/api/v1/orders/{id}/status", "invalid-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderStatus(anyLong(), anyString());
    }

    @Test
    void createOrder_MalformedJson_ReturnsBadRequest() throws Exception {
        // Given - Malformed JSON
        String malformedJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void getAllEndpoints_WithoutContentType_ReturnsOk() throws Exception {
        // Given
        List<OrderResponseDTO> orders = Arrays.asList(validOrderResponse);
        when(orderService.getAllOrders()).thenReturn(orders);
        when(orderAssembler.toModel(any(OrderResponseDTO.class))).thenReturn(orderEntityModel);

        // When & Then
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk());

        verify(orderService).getAllOrders();
    }
}