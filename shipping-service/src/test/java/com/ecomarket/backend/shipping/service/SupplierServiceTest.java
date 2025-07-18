package com.ecomarket.backend.shipping.service;

import com.ecomarket.backend.shipping.model.Supplier;
import com.ecomarket.backend.shipping.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier mockSupplier;
    private Supplier updatedSupplier;

    @BeforeEach
    void setUp() {
        // Setup mock supplier
        mockSupplier = new Supplier();
        mockSupplier.setSupplierId(1);
        mockSupplier.setName("Test Supplier");
        mockSupplier.setContactPerson("John Doe");
        mockSupplier.setPhone("+1234567890");
        mockSupplier.setEmail("john.doe@testsupplier.com");

        // Setup updated supplier data
        updatedSupplier = new Supplier();
        updatedSupplier.setSupplierId(1);
        updatedSupplier.setName("Updated Supplier");
        updatedSupplier.setContactPerson("Jane Smith");
        updatedSupplier.setPhone("+0987654321");
        updatedSupplier.setEmail("jane.smith@updatedsupplier.com");
    }

    @Test
    void createSupplier_Success() {
        // Given
        Supplier newSupplier = new Supplier();
        newSupplier.setName("New Supplier");
        newSupplier.setContactPerson("Alice Johnson");
        newSupplier.setPhone("+1122334455");
        newSupplier.setEmail("alice@newsupplier.com");

        when(supplierRepository.save(any(Supplier.class))).thenReturn(mockSupplier);

        // When
        Supplier result = supplierService.createSupplier(newSupplier);

        // Then
        assertNotNull(result);
        assertEquals(mockSupplier, result);
        verify(supplierRepository).save(newSupplier);
    }

    @Test
    void createSupplier_WithNullValues_Success() {
        // Given
        Supplier newSupplier = new Supplier();
        newSupplier.setName("Minimal Supplier");
        // contactPerson, phone, email pueden ser null según el DTO

        when(supplierRepository.save(any(Supplier.class))).thenReturn(newSupplier);

        // When
        Supplier result = supplierService.createSupplier(newSupplier);

        // Then
        assertNotNull(result);
        assertEquals(newSupplier, result);
        verify(supplierRepository).save(newSupplier);
    }

    @Test
    void getSupplierById_Success() {
        // Given
        when(supplierRepository.findById(1)).thenReturn(Optional.of(mockSupplier));

        // When
        Supplier result = supplierService.getSupplierById(1);

        // Then
        assertNotNull(result);
        assertEquals(mockSupplier, result);
        assertEquals(1, result.getSupplierId());
        assertEquals("Test Supplier", result.getName());
        assertEquals("John Doe", result.getContactPerson());
        assertEquals("+1234567890", result.getPhone());
        assertEquals("john.doe@testsupplier.com", result.getEmail());
        verify(supplierRepository).findById(1);
    }

    @Test
    void getSupplierById_NotFound_ThrowsException() {
        // Given
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> supplierService.getSupplierById(1)
        );

        assertEquals("Supplier not found with ID: 1", exception.getMessage());
        verify(supplierRepository).findById(1);
    }

    @Test
    void getAllSuppliers_Success() {
        // Given
        Supplier supplier2 = new Supplier();
        supplier2.setSupplierId(2);
        supplier2.setName("Second Supplier");
        supplier2.setContactPerson("Bob Wilson");
        supplier2.setPhone("+5566778899");
        supplier2.setEmail("bob@secondsupplier.com");

        List<Supplier> suppliers = Arrays.asList(mockSupplier, supplier2);
        when(supplierRepository.findAll()).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.getAllSuppliers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockSupplier, result.get(0));
        assertEquals(supplier2, result.get(1));
        verify(supplierRepository).findAll();
    }

    @Test
    void getAllSuppliers_EmptyList_Success() {
        // Given
        when(supplierRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Supplier> result = supplierService.getAllSuppliers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(supplierRepository).findAll();
    }

    @Test
    void updateSupplier_Success() {
        // Given
        when(supplierRepository.findById(1)).thenReturn(Optional.of(mockSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(mockSupplier);

        // When
        Supplier result = supplierService.updateSupplier(1, updatedSupplier);

        // Then
        assertNotNull(result);
        assertEquals(mockSupplier, result);

        // Verify that the existing supplier was updated with new values
        verify(supplierRepository).findById(1);
        verify(supplierRepository).save(mockSupplier);

    }

    @Test
    void updateSupplier_NotFound_ThrowsException() {
        // Given
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> supplierService.updateSupplier(1, updatedSupplier)
        );

        assertEquals("Supplier not found with ID: 1", exception.getMessage());
        verify(supplierRepository).findById(1);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_PartialUpdate_Success() {
        // Given
        Supplier partialUpdate = new Supplier();
        partialUpdate.setName("Partially Updated Supplier");
        partialUpdate.setContactPerson("New Contact");
        // phone and email remain null

        when(supplierRepository.findById(1)).thenReturn(Optional.of(mockSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(mockSupplier);

        // When
        Supplier result = supplierService.updateSupplier(1, partialUpdate);

        // Then
        assertNotNull(result);
        verify(supplierRepository).findById(1);
        verify(supplierRepository).save(mockSupplier);
    }

    @Test
    void deleteSupplier_Success() {
        // Given
        when(supplierRepository.existsById(1)).thenReturn(true);

        // When
        supplierService.deleteSupplier(1);

        // Then
        verify(supplierRepository).existsById(1);
        verify(supplierRepository).deleteById(1);
    }

    @Test
    void deleteSupplier_NotFound_ThrowsException() {
        // Given
        when(supplierRepository.existsById(1)).thenReturn(false);

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> supplierService.deleteSupplier(1)
        );

        assertEquals("Supplier not found with ID: 1", exception.getMessage());
        verify(supplierRepository).existsById(1);
        verify(supplierRepository, never()).deleteById(1);
    }

    @Test
    void deleteSupplier_MultipleIds_Success() {
        // Given
        when(supplierRepository.existsById(1)).thenReturn(true);
        when(supplierRepository.existsById(2)).thenReturn(true);

        // When
        supplierService.deleteSupplier(1);
        supplierService.deleteSupplier(2);

        // Then
        verify(supplierRepository).existsById(1);
        verify(supplierRepository).existsById(2);
        verify(supplierRepository).deleteById(1);
        verify(supplierRepository).deleteById(2);
    }

    @Test
    void updateSupplier_VerifyFieldUpdates() {
        // Given
        Supplier existingSupplier = new Supplier();
        existingSupplier.setSupplierId(1);
        existingSupplier.setName("Original Name");
        existingSupplier.setContactPerson("Original Contact");
        existingSupplier.setPhone("Original Phone");
        existingSupplier.setEmail("original@email.com");

        when(supplierRepository.findById(1)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier saved = invocation.getArgument(0);
            // Verify the fields were updated
            assertEquals("Updated Supplier", saved.getName());
            assertEquals("Jane Smith", saved.getContactPerson());
            assertEquals("+0987654321", saved.getPhone());
            assertEquals("jane.smith@updatedsupplier.com", saved.getEmail());
            return saved;
        });

        // When
        Supplier result = supplierService.updateSupplier(1, updatedSupplier);

        // Then
        assertNotNull(result);
        verify(supplierRepository).findById(1);
        verify(supplierRepository).save(existingSupplier);
    }
}
