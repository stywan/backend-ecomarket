package com.ecomarket.backend.auth.service;

import com.ecomarket.backend.auth.DTO.AddressRequest;
import com.ecomarket.backend.auth.DTO.AddressResponse;
import com.ecomarket.backend.auth.model.Address;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.repository.AddressRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private User otherUser;
    private AddressRequest testAddressRequest;
    private Address savedAddress;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .build();

        testAddressRequest = AddressRequest.builder()
                .street("Main St")
                .number("123")
                .commune("Springfield")
                .postalCode("12345")
                .build();

        savedAddress = Address.builder()
                .id(100L)
                .street("Main St")
                .number("123")
                .commune("Springfield")
                .postalCode("12345")
                .user(testUser)
                .build();
    }

    @Test
    void addAddress_shouldReturnAddressResponse_whenSuccessful() {
        when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);

        AddressResponse response = addressService.addAddress(testUser, testAddressRequest);

        assertNotNull(response);
        assertEquals(savedAddress.getId(), response.getId());
        assertEquals(testAddressRequest.getStreet(), response.getStreet());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void updateAddress_shouldReturnUpdatedAddressResponse_whenSuccessful() {
        AddressRequest updateRequest = AddressRequest.builder()
                .street("New Street")
                .number("456")
                .commune("New City")
                .postalCode("67890")
                .build();

        // Creamos una instancia de dirección existente para el mock
        Address existingAddress = Address.builder()
                .id(100L)
                .street("Old St")
                .number("111")
                .commune("Old City")
                .postalCode("00000")
                .user(testUser)
                .build();

        // Configura el findById para devolver la dirección existente
        when(addressRepository.findById(100L)).thenReturn(Optional.of(existingAddress));

        // Usa thenAnswer para que save devuelva la misma instancia de Address que recibió
        // Esto simula que los cambios se persisten en el objeto original
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address argAddress = invocation.getArgument(0); // Obtiene el argumento que se pasó a save
            return argAddress; // Devuelve esa misma instancia (con los cambios aplicados por el servicio)
        });

        AddressResponse response = addressService.updateAddress(testUser, 100L, updateRequest);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(updateRequest.getStreet(), response.getStreet());
        assertEquals(updateRequest.getNumber(), response.getNumber()); // Añadido para verificar el número
        assertEquals(updateRequest.getCommune(), response.getCommune());
        assertEquals(updateRequest.getPostalCode(), response.getPostalCode());
        verify(addressRepository, times(1)).findById(100L);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void updateAddress_shouldThrowException_whenAddressNotFound() {
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                addressService.updateAddress(testUser, 999L, testAddressRequest));

        assertEquals("Address not found", exception.getMessage());
        verify(addressRepository, times(1)).findById(999L);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void updateAddress_shouldThrowException_whenUnauthorizedUser() {
        Address existingAddress = Address.builder()
                .id(100L)
                .street("Old St")
                .number("111")
                .commune("Old City")
                .postalCode("00000")
                .user(otherUser) // Owned by other user
                .build();

        when(addressRepository.findById(100L)).thenReturn(Optional.of(existingAddress));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                addressService.updateAddress(testUser, 100L, testAddressRequest));

        assertEquals("Unauthorized", exception.getMessage());
        verify(addressRepository, times(1)).findById(100L);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void deleteAddress_shouldDeleteAddress_whenSuccessful() {
        when(addressRepository.findById(100L)).thenReturn(Optional.of(savedAddress));
        doNothing().when(addressRepository).delete(savedAddress);

        addressService.deleteAddress(testUser, 100L);

        verify(addressRepository, times(1)).findById(100L);
        verify(addressRepository, times(1)).delete(savedAddress);
    }

    @Test
    void deleteAddress_shouldThrowException_whenAddressNotFound() {
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                addressService.deleteAddress(testUser, 999L));

        assertEquals("Address not found", exception.getMessage());
        verify(addressRepository, times(1)).findById(999L);
        verify(addressRepository, never()).delete(any(Address.class));
    }

    @Test
    void deleteAddress_shouldThrowException_whenUnauthorizedUser() {
        Address existingAddress = Address.builder()
                .id(100L)
                .street("Old St")
                .number("111")
                .commune("Old City")
                .postalCode("00000")
                .user(otherUser) // Owned by other user
                .build();

        when(addressRepository.findById(100L)).thenReturn(Optional.of(existingAddress));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                addressService.deleteAddress(testUser, 100L));

        assertEquals("Unauthorized", exception.getMessage());
        verify(addressRepository, times(1)).findById(100L);
        verify(addressRepository, never()).delete(any(Address.class));
    }

    @Test
    void getUserAddresses_shouldReturnListOfAddresses_whenUserHasAddresses() {
        Address address1 = Address.builder().id(200L).street("User St 1").number("A").commune("C1").postalCode("P1").user(testUser).build();
        Address address2 = Address.builder().id(201L).street("User St 2").number("B").commune("C2").postalCode("P2").user(testUser).build();

        when(addressRepository.findByUser(testUser)).thenReturn(Arrays.asList(address1, address2));

        List<AddressResponse> responses = addressService.getUserAddresses(testUser);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(200L, responses.get(0).getId());
        assertEquals("User St 2", responses.get(1).getStreet());
        verify(addressRepository, times(1)).findByUser(testUser);
    }

    @Test
    void getUserAddresses_shouldReturnEmptyList_whenUserHasNoAddresses() {
        when(addressRepository.findByUser(testUser)).thenReturn(Arrays.asList());

        List<AddressResponse> responses = addressService.getUserAddresses(testUser);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(addressRepository, times(1)).findByUser(testUser);
    }
}
