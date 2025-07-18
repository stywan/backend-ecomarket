package com.ecomarket.backend.catalog_product.service;

import com.ecomarket.backend.catalog_product.DTO.request.InventoryOperationRequest;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.Inventory;
import com.ecomarket.backend.catalog_product.model.Product;
import com.ecomarket.backend.catalog_product.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepo;

    @InjectMocks
    private InventoryService inventoryService;

    // Datos de prueba comunes
    private Product testProduct;
    private Inventory initialInventory;
    private InventoryOperationRequest reserveRequest;
    private InventoryOperationRequest incrementRequest;
    private InventoryOperationRequest decrementRequest;
    private InventoryOperationRequest releaseRequest;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder().id(1L).name("Test Product").build();

        initialInventory = Inventory.builder()
                .id(100L)
                .product(testProduct)
                .availableQuantity(10)
                .lastUpdate(LocalDateTime.now().minusDays(1))
                .build();

        reserveRequest = InventoryOperationRequest.builder()
                .operationType("RESERVE")
                .quantity(5)
                .build();

        incrementRequest = InventoryOperationRequest.builder()
                .operationType("INCREMENT")
                .quantity(3)
                .build();

        decrementRequest = InventoryOperationRequest.builder()
                .operationType("DECREMENT")
                .quantity(2)
                .build();

        releaseRequest = InventoryOperationRequest.builder()
                .operationType("RELEASE")
                .quantity(4)
                .build();
    }

    @Test
    @DisplayName("handleOperation - Should throw IllegalArgumentException when reserving more than available stock")
    void handleOperation_reserveNotEnoughStock() {
        reserveRequest.setQuantity(15); // Try to reserve 15 from 10

        when(inventoryRepo.findByProduct_Id(anyLong())).thenReturn(Optional.of(initialInventory));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.handleOperation(testProduct.getId(), reserveRequest);
        });

        assertEquals("Not enough stock", thrown.getMessage());
        verify(inventoryRepo, times(1)).findByProduct_Id(testProduct.getId());
        verify(inventoryRepo, never()).save(any(Inventory.class)); // Save should not be called
    }

    @Test
    @DisplayName("handleOperation - Should increment quantity successfully")
    void handleOperation_incrementSuccess() {
        when(inventoryRepo.findByProduct_Id(anyLong())).thenReturn(Optional.of(initialInventory));
        when(inventoryRepo.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory result = inventoryService.handleOperation(testProduct.getId(), incrementRequest);

        assertNotNull(result);
        assertEquals(13, result.getAvailableQuantity()); // 10 + 3 = 13
        assertNotNull(result.getLastUpdate());
        verify(inventoryRepo, times(1)).findByProduct_Id(testProduct.getId());
        verify(inventoryRepo, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("handleOperation - Should decrement quantity successfully")
    void handleOperation_decrementSuccess() {
        when(inventoryRepo.findByProduct_Id(anyLong())).thenReturn(Optional.of(initialInventory));
        when(inventoryRepo.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory result = inventoryService.handleOperation(testProduct.getId(), decrementRequest);

        assertNotNull(result);
        assertEquals(8, result.getAvailableQuantity()); // 10 - 2 = 8
        assertNotNull(result.getLastUpdate());
        verify(inventoryRepo, times(1)).findByProduct_Id(testProduct.getId());
        verify(inventoryRepo, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("handleOperation - Should release quantity successfully")
    void handleOperation_releaseSuccess() {
        when(inventoryRepo.findByProduct_Id(anyLong())).thenReturn(Optional.of(initialInventory));
        when(inventoryRepo.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory result = inventoryService.handleOperation(testProduct.getId(), releaseRequest);

        assertNotNull(result);
        assertEquals(14, result.getAvailableQuantity());
        assertNotNull(result.getLastUpdate());
        verify(inventoryRepo, times(1)).findByProduct_Id(testProduct.getId());
        verify(inventoryRepo, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("handleOperation - Should throw IllegalArgumentException for invalid operation type")
    void handleOperation_invalidOperation() {
        InventoryOperationRequest invalidRequest = InventoryOperationRequest.builder()
                .operationType("UNKNOWN")
                .quantity(1)
                .build();

        when(inventoryRepo.findByProduct_Id(anyLong())).thenReturn(Optional.of(initialInventory)); // Still needs to find inventory

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.handleOperation(testProduct.getId(), invalidRequest);
        });

        assertEquals("Invalid operation", thrown.getMessage());
        verify(inventoryRepo, times(1)).findByProduct_Id(testProduct.getId());
        verify(inventoryRepo, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("handleOperation - Should throw ResourceNotFoundException when inventory is not found")
    void handleOperation_inventoryNotFound() {
        when(inventoryRepo.findByProduct_Id(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            inventoryService.handleOperation(99L, reserveRequest);
        });

        assertEquals("Inventory not found", thrown.getMessage());
        verify(inventoryRepo, times(1)).findByProduct_Id(99L);
        verify(inventoryRepo, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("getInventory - Should return inventory when found")
    void getInventory_success() {
        when(inventoryRepo.findByProduct_Id(anyLong())).thenReturn(Optional.of(initialInventory));

        Inventory result = inventoryService.getInventory(testProduct.getId());

        assertNotNull(result);
        assertEquals(initialInventory.getId(), result.getId());
        assertEquals(initialInventory.getAvailableQuantity(), result.getAvailableQuantity());
        verify(inventoryRepo, times(1)).findByProduct_Id(testProduct.getId());
    }

    @Test
    @DisplayName("getInventory - Should throw ResourceNotFoundException when inventory is not found")
    void getInventory_notFound() {
        when(inventoryRepo.findByProduct_Id(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            inventoryService.getInventory(99L);
        });

        assertEquals("Inventory not found", thrown.getMessage());
        verify(inventoryRepo, times(1)).findByProduct_Id(99L);
    }
}
