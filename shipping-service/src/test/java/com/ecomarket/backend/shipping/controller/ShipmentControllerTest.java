package com.ecomarket.backend.shipping.controller;

import com.ecomarket.backend.shipping.DTO.request.ShipmentRequestDTO;
import com.ecomarket.backend.shipping.DTO.response.ShipmentResponseDTO;
import com.ecomarket.backend.shipping.DTO.response.ShipmentStatusHistoryResponseDTO;
import com.ecomarket.backend.shipping.assembler.ShipmentAssembler;
import com.ecomarket.backend.shipping.assembler.ShipmentStatusHistoryAssembler;
import com.ecomarket.backend.shipping.model.Shipment;
import com.ecomarket.backend.shipping.model.ShipmentStatusHistory;
import com.ecomarket.backend.shipping.model.Supplier;
import com.ecomarket.backend.shipping.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShipmentService shipmentService;

    @MockitoBean
    private ShipmentAssembler shipmentAssembler;

    @MockitoBean
    private ShipmentStatusHistoryAssembler historyAssembler;

    private Shipment mockShipment;
    private ShipmentRequestDTO mockShipmentRequest;
    private EntityModel<ShipmentResponseDTO> mockEntityModel;

    @BeforeEach
    void setUp() {
        // Setup mock supplier
        Supplier mockSupplier = new Supplier();
        mockSupplier.setSupplierId(1);
        mockSupplier.setName("Test Supplier");
        mockSupplier.setContactPerson("John Doe");
        mockSupplier.setPhone("+1234567890");
        mockSupplier.setEmail("john.doe@testsupplier.com");

        // Setup mock shipment
        mockShipment = new Shipment();
        mockShipment.setShipmentId(1);
        mockShipment.setOrderId(101);
        mockShipment.setTrackingNumber("ECO-20250717-001");
        mockShipment.setSupplier(mockSupplier);
        mockShipment.setShipmentDate(LocalDateTime.now());
        mockShipment.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(3));
        mockShipment.setShipmentStatus(Shipment.ShipmentStatus.PENDING);
        mockShipment.setShippingCost(BigDecimal.valueOf(3990.0));
        mockShipment.setDestinationAddressId(1);

        // Setup mock request DTO
        mockShipmentRequest = new ShipmentRequestDTO();
        mockShipmentRequest.setOrderId(101L);
        mockShipmentRequest.setSupplierId(1);
        mockShipmentRequest.setTrackingNumber("ECO-20250717-001");

        // Setup mock response DTO
        ShipmentResponseDTO mockShipmentResponse = new ShipmentResponseDTO();
        mockShipmentResponse.setShipmentId(1);
        mockShipmentResponse.setOrderId(101);
        mockShipmentResponse.setTrackingNumber("ECO-20250717-001");
        mockShipmentResponse.setShipmentStatus(Shipment.ShipmentStatus.PENDING);
        mockShipmentResponse.setShippingCost(BigDecimal.valueOf(3990.0));
        mockShipmentResponse.setDestinationAddressId(1);

        // Setup mock entity model
        mockEntityModel = EntityModel.of(mockShipmentResponse);
    }


    @Test
    void createShipment_InvalidRequest_BadRequest() throws Exception {
        // Given - Invalid request with missing required fields
        ShipmentRequestDTO invalidRequest = new ShipmentRequestDTO();
        // Missing orderId, supplierId, trackingNumber

        // When & Then
        mockMvc.perform(post("/api/v1/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(shipmentService);
        verifyNoInteractions(shipmentAssembler);
    }

    @Test
    void createShipment_ServiceException_BadRequest() throws Exception {
        // Given
        when(shipmentService.createShipment(any(ShipmentRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Order not found with ID: 101"));

        // When & Then
        mockMvc.perform(post("/api/v1/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockShipmentRequest)))
                .andExpect(status().isBadRequest());

        verify(shipmentService).createShipment(any(ShipmentRequestDTO.class));
        verifyNoInteractions(shipmentAssembler);
    }

    @Test
    void getShipmentById_Success() throws Exception {
        // Given
        when(shipmentService.getShipmentById(1)).thenReturn(mockShipment);
        when(shipmentAssembler.toModel(any(Shipment.class))).thenReturn(mockEntityModel);

        // When & Then
        mockMvc.perform(get("/api/v1/shipments/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shipmentId").value(1))
                .andExpect(jsonPath("$.orderId").value(101))
                .andExpect(jsonPath("$.trackingNumber").value("ECO-20250717-001"));

        verify(shipmentService).getShipmentById(1);
        verify(shipmentAssembler).toModel(mockShipment);
    }

    @Test
    void getShipmentById_NotFound() throws Exception {
        // Given
        when(shipmentService.getShipmentById(1))
                .thenThrow(new EntityNotFoundException("Shipment not found with ID: 1"));

        // When & Then
        mockMvc.perform(get("/api/v1/shipments/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(shipmentService).getShipmentById(1);
        verifyNoInteractions(shipmentAssembler);
    }

    @Test
    void getAllShipments_Success() throws Exception {
        // Given
        List<Shipment> shipments = Collections.singletonList(mockShipment);
        CollectionModel<EntityModel<ShipmentResponseDTO>> collectionModel =
                CollectionModel.of(Collections.singletonList(mockEntityModel));

        when(shipmentService.getAllShipments()).thenReturn(shipments);
        when(shipmentAssembler.toCollectionModel(shipments)).thenReturn(collectionModel);

        // When & Then
        mockMvc.perform(get("/api/v1/shipments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(shipmentService).getAllShipments();
        verify(shipmentAssembler).toCollectionModel(shipments);
    }

    @Test
    void getAllShipments_EmptyList() throws Exception {
        // Given
        List<Shipment> emptyList = Collections.emptyList();
        CollectionModel<EntityModel<ShipmentResponseDTO>> emptyCollectionModel =
                CollectionModel.of(Collections.emptyList());

        when(shipmentService.getAllShipments()).thenReturn(emptyList);
        when(shipmentAssembler.toCollectionModel(emptyList)).thenReturn(emptyCollectionModel);

        // When & Then
        mockMvc.perform(get("/api/v1/shipments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(shipmentService).getAllShipments();
        verify(shipmentAssembler).toCollectionModel(emptyList);
    }



    @Test
    void updateShipment_NotFound() throws Exception {
        // Given
        when(shipmentService.updateShipment(eq(1), any(ShipmentRequestDTO.class)))
                .thenThrow(new EntityNotFoundException("Shipment not found with ID: 1"));

        // When & Then
        mockMvc.perform(put("/api/v1/shipments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockShipmentRequest)))
                .andExpect(status().isNotFound());

        verify(shipmentService).updateShipment(eq(1), any(ShipmentRequestDTO.class));
        verifyNoInteractions(shipmentAssembler);
    }

    @Test
    void updateShipmentStatus_Success() throws Exception {
        // Given
        when(shipmentService.updateShipmentStatus(1, Shipment.ShipmentStatus.DELIVERED, "Package shipped"))
                .thenReturn(mockShipment);
        when(shipmentAssembler.toModel(any(Shipment.class))).thenReturn(mockEntityModel);

        // When & Then
        mockMvc.perform(patch("/api/v1/shipments/1/status")
                        .param("newStatus", "DELIVERED")
                        .param("notes", "Package shipped")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shipmentId").value(1));

        verify(shipmentService).updateShipmentStatus(1, Shipment.ShipmentStatus.DELIVERED, "Package shipped");
        verify(shipmentAssembler).toModel(mockShipment);
    }

    @Test
    void updateShipmentStatus_WithoutNotes_Success() throws Exception {
        // Given
        when(shipmentService.updateShipmentStatus(1, Shipment.ShipmentStatus.DELIVERED, null))
                .thenReturn(mockShipment);
        when(shipmentAssembler.toModel(any(Shipment.class))).thenReturn(mockEntityModel);

        // When & Then
        mockMvc.perform(patch("/api/v1/shipments/1/status")
                        .param("newStatus", "DELIVERED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(shipmentService).updateShipmentStatus(1, Shipment.ShipmentStatus.DELIVERED, null);
        verify(shipmentAssembler).toModel(mockShipment);
    }

    @Test
    void updateShipmentStatus_InvalidStatus_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/v1/shipments/1/status")
                        .param("newStatus", "INVALID_STATUS")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(shipmentService);
        verifyNoInteractions(shipmentAssembler);
    }

    @Test
    void updateShipmentStatus_NotFound() throws Exception {
        // Given
        when(shipmentService.updateShipmentStatus(1, Shipment.ShipmentStatus.DELIVERED, null))
                .thenThrow(new EntityNotFoundException("Shipment not found with ID: 1"));

        // When & Then
        mockMvc.perform(patch("/api/v1/shipments/1/status")
                        .param("newStatus", "DELIVERED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(shipmentService).updateShipmentStatus(1, Shipment.ShipmentStatus.DELIVERED, null);
        verifyNoInteractions(shipmentAssembler);
    }

    @Test
    void deleteShipment_Success() throws Exception {
        // Given
        doNothing().when(shipmentService).deleteShipment(1);

        // When & Then
        mockMvc.perform(delete("/api/v1/shipments/1"))
                .andExpect(status().isNoContent());

        verify(shipmentService).deleteShipment(1);
    }

    @Test
    void deleteShipment_NotFound() throws Exception {
        // Given
        doThrow(new EntityNotFoundException("Shipment not found with ID: 1"))
                .when(shipmentService).deleteShipment(1);

        // When & Then
        mockMvc.perform(delete("/api/v1/shipments/1"))
                .andExpect(status().isNotFound());

        verify(shipmentService).deleteShipment(1);
    }

    @Test
    void getShipmentStatusHistory_Success() throws Exception {
        // Given
        ShipmentStatusHistory history1 = new ShipmentStatusHistory();
        history1.setHistoryId(1);
        history1.setShipment(mockShipment);
        history1.setStatusDescription("PENDING");
        history1.setNotes("Initial status");
        history1.setEventDatetime(LocalDateTime.now());

        List<ShipmentStatusHistory> historyList = List.of(history1);

        ShipmentStatusHistoryResponseDTO historyResponse = new ShipmentStatusHistoryResponseDTO();
        historyResponse.setHistoryId(1);
        historyResponse.setStatusDescription("PENDING");
        historyResponse.setNotes("Initial status");

        EntityModel<ShipmentStatusHistoryResponseDTO> historyEntityModel = EntityModel.of(historyResponse);
        CollectionModel<EntityModel<ShipmentStatusHistoryResponseDTO>> historyCollectionModel =
                CollectionModel.of(Collections.singletonList(historyEntityModel));

        when(shipmentService.getShipmentStatusHistory(1)).thenReturn(historyList);
        when(historyAssembler.toCollectionModel(historyList)).thenReturn(historyCollectionModel);

        // When & Then
        mockMvc.perform(get("/api/v1/shipments/1/history")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(shipmentService).getShipmentStatusHistory(1);
        verify(historyAssembler).toCollectionModel(historyList);
    }

    @Test
    void getShipmentStatusHistory_NotFound() throws Exception {
        // Given
        when(shipmentService.getShipmentStatusHistory(1))
                .thenThrow(new EntityNotFoundException("Shipment not found with ID: 1"));

        // When & Then
        mockMvc.perform(get("/api/v1/shipments/1/history")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(shipmentService).getShipmentStatusHistory(1);
        verifyNoInteractions(historyAssembler);
    }

    @Test
    void getShipmentStatusHistory_EmptyHistory() throws Exception {
        // Given
        List<ShipmentStatusHistory> emptyHistory = Collections.emptyList();
        CollectionModel<EntityModel<ShipmentStatusHistoryResponseDTO>> emptyCollectionModel =
                CollectionModel.of(Collections.emptyList());

        when(shipmentService.getShipmentStatusHistory(1)).thenReturn(emptyHistory);
        when(historyAssembler.toCollectionModel(emptyHistory)).thenReturn(emptyCollectionModel);

        // When & Then
        mockMvc.perform(get("/api/v1/shipments/1/history")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(shipmentService).getShipmentStatusHistory(1);
        verify(historyAssembler).toCollectionModel(emptyHistory);
    }
}
