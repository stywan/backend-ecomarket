package com.ecomarket.backend.shipping.controller;

import com.ecomarket.backend.shipping.DTO.request.SupplierRequestDTO;
import com.ecomarket.backend.shipping.DTO.response.SupplierResponseDTO;
import com.ecomarket.backend.shipping.assembler.SupplierAssembler;
import com.ecomarket.backend.shipping.model.Supplier;
import com.ecomarket.backend.shipping.service.SupplierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SupplierController.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierService supplierService;

    @MockitoBean
    private SupplierAssembler supplierAssembler;

    private Supplier mockSupplier;
    private SupplierRequestDTO mockSupplierRequestDTO;
    private SupplierResponseDTO mockSupplierResponseDTO;

    @BeforeEach
    void setUp() {
        mockSupplier = new Supplier();
        mockSupplier.setSupplierId(1);
        mockSupplier.setName("Test Supplier");
        mockSupplier.setContactPerson("John Doe");
        mockSupplier.setPhone("+56912345678");
        mockSupplier.setEmail("test@supplier.com");

        mockSupplierRequestDTO = new SupplierRequestDTO();
        mockSupplierRequestDTO.setName("Test Supplier");
        mockSupplierRequestDTO.setContactPerson("John Doe");
        mockSupplierRequestDTO.setPhone("+56912345678");
        mockSupplierRequestDTO.setEmail("test@supplier.com");

        mockSupplierResponseDTO = new SupplierResponseDTO(
                mockSupplier.getSupplierId(),
                mockSupplier.getName(),
                mockSupplier.getContactPerson(),
                mockSupplier.getPhone(),
                mockSupplier.getEmail()
        );

        EntityModel<SupplierResponseDTO> mockEntityModel = EntityModel.of(mockSupplierResponseDTO,
                linkTo(methodOn(SupplierController.class).getSupplierById(mockSupplier.getSupplierId())).withSelfRel(),
                linkTo(methodOn(SupplierController.class).getAllSuppliers()).withRel("allSuppliers"));

        when(supplierAssembler.toModel(any(Supplier.class))).thenReturn(mockEntityModel);
    }



    @Test
    @DisplayName("POST /api/v1/suppliers - Should return 400 Bad Request for invalid input")
    void createSupplier_InvalidRequest_BadRequest() throws Exception {
        SupplierRequestDTO invalidRequest = new SupplierRequestDTO();
        invalidRequest.setName("");
        invalidRequest.setContactPerson("No name");
        invalidRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Validation failed:"))); // From GlobalExceptionHandler for MethodArgumentNotValidException

        verifyNoInteractions(supplierService);
        verifyNoInteractions(supplierAssembler);
    }

    @Test
    @DisplayName("GET /api/v1/suppliers/{id} - Should retrieve supplier and return 200 OK")
    void getSupplierById_Success() throws Exception {
        when(supplierService.getSupplierById(1)).thenReturn(mockSupplier);

        mockMvc.perform(get("/api/v1/suppliers/{id}", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.supplierId").value(mockSupplier.getSupplierId()))
                .andExpect(jsonPath("$.name").value(mockSupplier.getName()))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/v1/suppliers/1")));

        verify(supplierService).getSupplierById(1);
        verify(supplierAssembler).toModel(mockSupplier);
    }

    @Test
    @DisplayName("GET /api/v1/suppliers/{id} - Should return 404 Not Found if supplier does not exist")
    void getSupplierById_NotFound() throws Exception {
        when(supplierService.getSupplierById(99))
                .thenThrow(new EntityNotFoundException("Supplier not found with ID: 99"));

        mockMvc.perform(get("/api/v1/suppliers/{id}", 99)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Supplier not found with ID: 99")));

        verify(supplierService).getSupplierById(99);
        verifyNoInteractions(supplierAssembler);
    }

    @Test
    @DisplayName("GET /api/v1/suppliers - Should retrieve all suppliers and return 200 OK")
    void getAllSuppliers_Success() throws Exception {
        Supplier mockSupplier2 = new Supplier(2, "Another Supplier", "Jane Doe", "+56987654321", "jane@supplier.com");
        List<Supplier> suppliers = Arrays.asList(mockSupplier, mockSupplier2);

        EntityModel<SupplierResponseDTO> model1 = EntityModel.of(mockSupplierResponseDTO,
                linkTo(methodOn(SupplierController.class).getSupplierById(mockSupplier.getSupplierId())).withSelfRel());
        EntityModel<SupplierResponseDTO> model2 = EntityModel.of(
                new SupplierResponseDTO(mockSupplier2.getSupplierId(), mockSupplier2.getName(), mockSupplier2.getContactPerson(), mockSupplier2.getPhone(), mockSupplier2.getEmail()),
                linkTo(methodOn(SupplierController.class).getSupplierById(mockSupplier2.getSupplierId())).withSelfRel());

        CollectionModel<EntityModel<SupplierResponseDTO>> collectionModel = CollectionModel.of(
                Arrays.asList(model1, model2),
                linkTo(methodOn(SupplierController.class).getAllSuppliers()).withSelfRel()
        );

        when(supplierService.getAllSuppliers()).thenReturn(suppliers);
        when(supplierAssembler.toCollectionModel(suppliers)).thenReturn(collectionModel);

        mockMvc.perform(get("/api/v1/suppliers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.supplierResponseDTOList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.supplierResponseDTOList[0].supplierId", is(mockSupplier.getSupplierId())))
                .andExpect(jsonPath("$._embedded.supplierResponseDTOList[1].name", is(mockSupplier2.getName())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/v1/suppliers")));

        verify(supplierService).getAllSuppliers();
        verify(supplierAssembler).toCollectionModel(suppliers);
    }

    @Test
    @DisplayName("GET /api/v1/suppliers - Should return 200 OK with empty list if no suppliers exist")
    void getAllSuppliers_EmptyList() throws Exception {
        // Given: An empty list of suppliers
        List<Supplier> emptyList = Collections.emptyList();
        CollectionModel<EntityModel<SupplierResponseDTO>> emptyCollectionModel = CollectionModel.of(
                Collections.emptyList(),
                linkTo(methodOn(SupplierController.class).getAllSuppliers()).withSelfRel()
        );

        when(supplierService.getAllSuppliers()).thenReturn(emptyList);
        when(supplierAssembler.toCollectionModel(emptyList)).thenReturn(emptyCollectionModel);

        mockMvc.perform(get("/api/v1/suppliers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded").doesNotExist());
        verify(supplierService).getAllSuppliers();
        verify(supplierAssembler).toCollectionModel(emptyList);
    }

    @Test
    @DisplayName("PUT /api/v1/suppliers/{id} - Should return 404 Not Found if supplier to update does not exist")
    void updateSupplier_NotFound() throws Exception {
        when(supplierService.updateSupplier(eq(99), any(Supplier.class)))
                .thenThrow(new EntityNotFoundException("Supplier not found with ID: 99"));

        mockMvc.perform(put("/api/v1/suppliers/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockSupplierRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Supplier not found with ID: 99")));

        verify(supplierService).updateSupplier(eq(99), any(Supplier.class));
        verifyNoInteractions(supplierAssembler);
    }

    @Test
    @DisplayName("DELETE /api/v1/suppliers/{id} - Should delete supplier and return 204 No Content")
    void deleteSupplier_Success() throws Exception {
        doNothing().when(supplierService).deleteSupplier(1);

        mockMvc.perform(delete("/api/v1/suppliers/{id}", 1))
                .andExpect(status().isNoContent());

        verify(supplierService).deleteSupplier(1);
    }

    @Test
    @DisplayName("DELETE /api/v1/suppliers/{id} - Should return 404 Not Found if supplier to delete does not exist")
    void deleteSupplier_NotFound() throws Exception {
        doThrow(new EntityNotFoundException("Supplier not found with ID: 99"))
                .when(supplierService).deleteSupplier(99);

        mockMvc.perform(delete("/api/v1/suppliers/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Supplier not found with ID: 99")));

        verify(supplierService).deleteSupplier(99);
    }
}
