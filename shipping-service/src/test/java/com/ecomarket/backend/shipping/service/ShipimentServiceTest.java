package com.ecomarket.backend.shipping.service;

import com.ecomarket.backend.shipping.DTO.request.ShipmentRequestDTO;
import com.ecomarket.backend.shipping.DTO.response.OrderResponseDTO;
import com.ecomarket.backend.shipping.client.OrderServiceClient;
import com.ecomarket.backend.shipping.model.Shipment;
import com.ecomarket.backend.shipping.model.ShipmentStatusHistory;
import com.ecomarket.backend.shipping.model.Supplier;
import com.ecomarket.backend.shipping.repository.ShipmentRepository;
import com.ecomarket.backend.shipping.repository.ShipmentStatusHistoryRepository;
import com.ecomarket.backend.shipping.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private OrderServiceClient orderServiceClient;

    @Mock
    private ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    private Supplier mockSupplier;
    private OrderResponseDTO mockOrderResponse;
    private ShipmentRequestDTO mockShipmentRequest;
    private Shipment mockShipment;

    @BeforeEach
    void setUp() {
        // Setup mock supplier
        mockSupplier = new Supplier();
        mockSupplier.setSupplierId(1);
        mockSupplier.setName("Test Supplier");
        mockSupplier.setContactPerson("John Doe");
        mockSupplier.setPhone("+1234567890");
        mockSupplier.setEmail("john.doe@testsupplier.com");

        // Setup mock order response
        mockOrderResponse = new OrderResponseDTO();
        mockOrderResponse.setId(1L);
        mockOrderResponse.setShippingAddressId(100L);

        // Setup mock shipment request
        mockShipmentRequest = new ShipmentRequestDTO();
        mockShipmentRequest.setOrderId(1L);
        mockShipmentRequest.setSupplierId(1);
        mockShipmentRequest.setTrackingNumber("TRK123456");

        // Setup mock shipment
        mockShipment = new Shipment();
        mockShipment.setShipmentId(1);
        mockShipment.setOrderId(1);
        mockShipment.setTrackingNumber("TRK123456");
        mockShipment.setSupplier(mockSupplier);
        mockShipment.setShipmentStatus(Shipment.ShipmentStatus.PENDING);
        mockShipment.setShippingCost(BigDecimal.valueOf(3990.0));
        mockShipment.setDestinationAddressId(100);
        mockShipment.setShipmentDate(LocalDateTime.now());
        mockShipment.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(3));
    }

    @Test
    void createShipment_Success() {
        // Given
        when(supplierRepository.findById(1)).thenReturn(Optional.of(mockSupplier));
        when(orderServiceClient.getOrderById(1L)).thenReturn(mockOrderResponse);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(mockShipment);

        // When
        Shipment result = shipmentService.createShipment(mockShipmentRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals("TRK123456", result.getTrackingNumber());
        assertEquals(mockSupplier, result.getSupplier());
        assertEquals(Shipment.ShipmentStatus.PENDING, result.getShipmentStatus());
        assertEquals(BigDecimal.valueOf(3990.0), result.getShippingCost());
        assertEquals(100, result.getDestinationAddressId());

        verify(supplierRepository).findById(1);
        verify(orderServiceClient).getOrderById(1L);
        verify(shipmentRepository).save(any(Shipment.class));
        verify(shipmentStatusHistoryRepository).save(any(ShipmentStatusHistory.class));
    }

    @Test
    void createShipment_SupplierNotFound_ThrowsException() {
        // Given
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shipmentService.createShipment(mockShipmentRequest)
        );

        assertEquals("Supplier not found with ID: 1", exception.getMessage());
        verify(supplierRepository).findById(1);
        verifyNoInteractions(orderServiceClient);
        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void createShipment_OrderNotFound_ThrowsException() {
        // Given
        when(supplierRepository.findById(1)).thenReturn(Optional.of(mockSupplier));
        when(orderServiceClient.getOrderById(1L)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shipmentService.createShipment(mockShipmentRequest)
        );

        assertEquals("Order not found with ID: 1", exception.getMessage());
        verify(supplierRepository).findById(1);
        verify(orderServiceClient).getOrderById(1L);
        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void createShipment_MissingShippingAddress_ThrowsException() {
        // Given
        mockOrderResponse.setShippingAddressId(null);
        when(supplierRepository.findById(1)).thenReturn(Optional.of(mockSupplier));
        when(orderServiceClient.getOrderById(1L)).thenReturn(mockOrderResponse);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shipmentService.createShipment(mockShipmentRequest)
        );

        assertEquals("Shipping Address ID is missing in the Order with ID: 1", exception.getMessage());
        verify(supplierRepository).findById(1);
        verify(orderServiceClient).getOrderById(1L);
        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void updateShipment_Success() {
        // Given
        when(shipmentRepository.findById(1)).thenReturn(Optional.of(mockShipment));
        when(supplierRepository.findById(1)).thenReturn(Optional.of(mockSupplier));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(mockShipment);

        // When
        Shipment result = shipmentService.updateShipment(1, mockShipmentRequest);

        // Then
        assertNotNull(result);
        assertEquals("TRK123456", result.getTrackingNumber());
        assertEquals(mockSupplier, result.getSupplier());

        verify(shipmentRepository).findById(1);
        verify(supplierRepository).findById(1);
        verify(shipmentRepository).save(mockShipment);
    }

    @Test
    void updateShipment_ShipmentNotFound_ThrowsException() {
        // Given
        when(shipmentRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> shipmentService.updateShipment(1, mockShipmentRequest)
        );

        assertEquals("Shipment not found with ID: 1", exception.getMessage());
        verify(shipmentRepository).findById(1);
        verifyNoInteractions(supplierRepository);
    }

    @Test
    void updateShipment_SupplierNotFound_ThrowsException() {
        // Given
        when(shipmentRepository.findById(1)).thenReturn(Optional.of(mockShipment));
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shipmentService.updateShipment(1, mockShipmentRequest)
        );

        assertEquals("Supplier not found with ID: 1", exception.getMessage());
        verify(shipmentRepository).findById(1);
        verify(supplierRepository).findById(1);
    }

    @Test
    void updateShipmentStatus_Success() {
        // Given
        Shipment.ShipmentStatus newStatus = Shipment.ShipmentStatus.DELIVERED;
        String notes = "Package shipped successfully";

        when(shipmentRepository.findById(1)).thenReturn(Optional.of(mockShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(mockShipment);

        // When
        Shipment result = shipmentService.updateShipmentStatus(1, newStatus, notes);

        // Then
        assertNotNull(result);
        verify(shipmentRepository).findById(1);
        verify(shipmentRepository).save(mockShipment);
        verify(shipmentStatusHistoryRepository).save(any(ShipmentStatusHistory.class));
    }

    @Test
    void updateShipmentStatus_SameStatus_NoUpdate() {
        // Given
        Shipment.ShipmentStatus currentStatus = Shipment.ShipmentStatus.PENDING;
        mockShipment.setShipmentStatus(currentStatus);

        when(shipmentRepository.findById(1)).thenReturn(Optional.of(mockShipment));

        // When
        Shipment result = shipmentService.updateShipmentStatus(1, currentStatus, "Test notes");

        // Then
        assertNotNull(result);
        verify(shipmentRepository).findById(1);
        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(shipmentStatusHistoryRepository, never()).save(any(ShipmentStatusHistory.class));
    }

    @Test
    void updateShipmentStatus_ShipmentNotFound_ThrowsException() {
        // Given
        when(shipmentRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> shipmentService.updateShipmentStatus(1, Shipment.ShipmentStatus.DELIVERED, "Test notes")
        );

        assertEquals("Shipment not found with ID: 1", exception.getMessage());
        verify(shipmentRepository).findById(1);
        verifyNoInteractions(shipmentStatusHistoryRepository);
    }

    @Test
    void getShipmentById_Success() {
        // Given
        when(shipmentRepository.findById(1)).thenReturn(Optional.of(mockShipment));

        // When
        Shipment result = shipmentService.getShipmentById(1);

        // Then
        assertNotNull(result);
        assertEquals(mockShipment, result);
        verify(shipmentRepository).findById(1);
    }

    @Test
    void getShipmentById_NotFound_ThrowsException() {
        // Given
        when(shipmentRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> shipmentService.getShipmentById(1)
        );

        assertEquals("Shipment not found with ID: 1", exception.getMessage());
        verify(shipmentRepository).findById(1);
    }

    @Test
    void getAllShipments_Success() {
        // Given
        List<Shipment> shipments = Arrays.asList(mockShipment, new Shipment());
        when(shipmentRepository.findAll()).thenReturn(shipments);

        // When
        List<Shipment> result = shipmentService.getAllShipments();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(shipmentRepository).findAll();
    }

    @Test
    void deleteShipment_Success() {
        // Given
        when(shipmentRepository.existsById(1)).thenReturn(true);

        // When
        shipmentService.deleteShipment(1);

        // Then
        verify(shipmentRepository).existsById(1);
        verify(shipmentRepository).deleteById(1);
    }

    @Test
    void deleteShipment_NotFound_ThrowsException() {
        // Given
        when(shipmentRepository.existsById(1)).thenReturn(false);

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> shipmentService.deleteShipment(1)
        );

        assertEquals("Shipment not found with ID: 1", exception.getMessage());
        verify(shipmentRepository).existsById(1);
        verify(shipmentRepository, never()).deleteById(1);
    }

    @Test
    void getShipmentStatusHistory_Success() {
        // Given
        List<ShipmentStatusHistory> history = Arrays.asList(
                new ShipmentStatusHistory(),
                new ShipmentStatusHistory()
        );
        when(shipmentRepository.existsById(1)).thenReturn(true);
        when(shipmentStatusHistoryRepository.findByShipmentShipmentIdOrderByEventDatetimeAsc(1))
                .thenReturn(history);

        // When
        List<ShipmentStatusHistory> result = shipmentService.getShipmentStatusHistory(1);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(shipmentRepository).existsById(1);
        verify(shipmentStatusHistoryRepository).findByShipmentShipmentIdOrderByEventDatetimeAsc(1);
    }

    @Test
    void getShipmentStatusHistory_ShipmentNotFound_ThrowsException() {
        // Given
        when(shipmentRepository.existsById(1)).thenReturn(false);

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> shipmentService.getShipmentStatusHistory(1)
        );

        assertEquals("Shipment not found with ID: 1", exception.getMessage());
        verify(shipmentRepository).existsById(1);
        verifyNoInteractions(shipmentStatusHistoryRepository);
    }

    @Test
    void createShipmentStatusHistoryEntry_Success() {
        // Given
        String statusDescription = "PENDING";
        String notes = "Test notes";

        // When
        shipmentService.createShipmentStatusHistoryEntry(mockShipment, statusDescription, notes);

        // Then
        verify(shipmentStatusHistoryRepository).save(any(ShipmentStatusHistory.class));
    }
}