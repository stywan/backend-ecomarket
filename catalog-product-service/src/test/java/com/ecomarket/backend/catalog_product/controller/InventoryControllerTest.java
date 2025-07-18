package com.ecomarket.backend.catalog_product.controller;

import com.ecomarket.backend.catalog_product.DTO.request.InventoryOperationRequest;
import com.ecomarket.backend.catalog_product.DTO.response.InventoryResponse;
import com.ecomarket.backend.catalog_product.assembler.InventoryAssembler;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.Inventory;
import com.ecomarket.backend.catalog_product.model.Product;
import com.ecomarket.backend.catalog_product.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@DisplayName("InventoryController Unit Tests")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private InventoryAssembler inventoryAssembler;

    // Datos de prueba comunes
    private Product testProduct;
    private Inventory testInventory;
    private InventoryResponse testInventoryResponse;
    private InventoryOperationRequest reserveRequest;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder().id(1L).name("Laptop").build();

        testInventory = Inventory.builder()
                .id(100L)
                .product(testProduct)
                .availableQuantity(10)
                .location("Warehouse A")
                .lastUpdate(LocalDateTime.now())
                .build();

        testInventoryResponse = InventoryResponse.builder()
                .productId(1L)
                .availableQuantity(10)
                .location("Warehouse A")
                .lastUpdate(testInventory.getLastUpdate())
                .build();

        // Configurar el assembler mock
        EntityModel<InventoryResponse> testInventoryEntityModel = EntityModel.of(testInventoryResponse,
                linkTo(methodOn(InventoryController.class).getInventory(testProduct.getId())).withSelfRel());

        when(inventoryAssembler.toModel(any(Inventory.class))).thenReturn(testInventoryEntityModel);

        reserveRequest = InventoryOperationRequest.builder()
                .operationType("RESERVE")
                .quantity(5)
                .location("Warehouse A")
                .build();

        InventoryOperationRequest incrementRequest = InventoryOperationRequest.builder()
                .operationType("INCREMENT")
                .quantity(3)
                .location("Warehouse A")
                .build();
    }

    // --- Tests para GET /api/v1/inventory/{productId} (getInventory) ---
    @Test
    @DisplayName("GET /{productId} - Should return inventory when found")
    void getInventory_success() throws Exception {
        when(inventoryService.getInventory(anyLong())).thenReturn(testInventory);

        mockMvc.perform(get("/api/v1/inventory/{productId}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.availableQuantity").value(testInventoryResponse.getAvailableQuantity()))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(inventoryService, times(1)).getInventory(testProduct.getId());
        verify(inventoryAssembler, times(1)).toModel(testInventory);
    }

    @Test
    @DisplayName("GET /{productId} - Should return 404 Not Found when inventory not found")
    void getInventory_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Inventory not found")).when(inventoryService).getInventory(anyLong());

        mockMvc.perform(get("/api/v1/inventory/{productId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Inventory not found"));

        verify(inventoryService, times(1)).getInventory(99L);
        verify(inventoryAssembler, never()).toModel(any(Inventory.class)); // Assembler should not be called
    }

    // --- Tests para POST /api/v1/inventory/{productId}/operation (handleOperation) ---
    @Test
    @DisplayName("POST /{productId}/operation - Should handle operation successfully")
    void handleOperation_success() throws Exception {
        Inventory updatedInventory = Inventory.builder()
                .id(100L)
                .product(testProduct)
                .availableQuantity(5)
                .location("Warehouse A")
                .lastUpdate(LocalDateTime.now())
                .build();
        InventoryResponse updatedResponse = InventoryResponse.builder()
                .productId(1L)
                .availableQuantity(5)
                .location("Warehouse A")
                .lastUpdate(updatedInventory.getLastUpdate())
                .build();
        EntityModel<InventoryResponse> updatedEntityModel = EntityModel.of(updatedResponse,
                linkTo(methodOn(InventoryController.class).getInventory(testProduct.getId())).withSelfRel());

        when(inventoryService.handleOperation(anyLong(), any(InventoryOperationRequest.class))).thenReturn(updatedInventory);
        when(inventoryAssembler.toModel(updatedInventory)).thenReturn(updatedEntityModel);

        mockMvc.perform(post("/api/v1/inventory/{productId}/operation", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.availableQuantity").value(5));

        verify(inventoryService, times(1)).handleOperation(testProduct.getId(), reserveRequest);
        verify(inventoryAssembler, times(1)).toModel(updatedInventory);
    }

    @Test
    @DisplayName("POST /{productId}/operation - Should return 400 Bad Request for invalid request DTO (e.g., null quantity)")
    void handleOperation_invalidRequestDTO() throws Exception {
        InventoryOperationRequest invalidRequest = InventoryOperationRequest.builder()
                .operationType("RESERVE")
                .quantity(null) // Invalid: quantity is @NotNull
                .location("Warehouse A")
                .build();

        mockMvc.perform(post("/api/v1/inventory/{productId}/operation", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists()); // Check for validation error message

        verify(inventoryService, never()).handleOperation(anyLong(), any(InventoryOperationRequest.class));
    }

    @Test
    @DisplayName("POST /{productId}/operation - Should return 400 Bad Request for not enough stock (service throws IllegalArgumentException)")
    void handleOperation_notEnoughStock() throws Exception {
        // Simulate service throwing IllegalArgumentException due to business logic
        doThrow(new IllegalArgumentException("Not enough stock")).when(inventoryService)
                .handleOperation(anyLong(), any(InventoryOperationRequest.class));

        mockMvc.perform(post("/api/v1/inventory/{productId}/operation", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveRequest))) // Requesting 5 from 10, but mock throws error
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Not enough stock"));

        verify(inventoryService, times(1)).handleOperation(testProduct.getId(), reserveRequest);
        verify(inventoryAssembler, never()).toModel(any(Inventory.class));
    }

    @Test
    @DisplayName("POST /{productId}/operation - Should return 404 Not Found when inventory not found for operation")
    void handleOperation_inventoryNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Inventory not found")).when(inventoryService)
                .handleOperation(anyLong(), any(InventoryOperationRequest.class));

        mockMvc.perform(post("/api/v1/inventory/{productId}/operation", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Inventory not found"));

        verify(inventoryService, times(1)).handleOperation(99L, reserveRequest);
        verify(inventoryAssembler, never()).toModel(any(Inventory.class));
    }

    @Test
    @DisplayName("POST /{productId}/operation - Should return 400 Bad Request for invalid operation type (service throws IllegalArgumentException)")
    void handleOperation_invalidOperationType() throws Exception {
        InventoryOperationRequest invalidOperationRequest = InventoryOperationRequest.builder()
                .operationType("INVALID_TYPE")
                .quantity(1)
                .location("Warehouse A")
                .build();

        doThrow(new IllegalArgumentException("Invalid operation")).when(inventoryService)
                .handleOperation(anyLong(), any(InventoryOperationRequest.class));

        mockMvc.perform(post("/api/v1/inventory/{productId}/operation", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOperationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid operation"));

        verify(inventoryService, times(1)).handleOperation(testProduct.getId(), invalidOperationRequest);
        verify(inventoryAssembler, never()).toModel(any(Inventory.class));
    }
}
