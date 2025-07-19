package com.ecomarket.backend.cart_order.service;
import com.ecomarket.backend.cart_order.DTO.response.UserResponseDTO;
import com.ecomarket.backend.cart_order.exception.ResourceNotFoundException;
import com.ecomarket.backend.cart_order.repository.AddressRepository;
import com.ecomarket.backend.cart_order.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private UserService userService;

    private UserResponseDTO validUserWithoutAddress;
    private Long validUserId;
    private Long validAddressId;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        validUserId = 1L;
        validAddressId = 100L;

        validUserWithoutAddress = UserResponseDTO.builder()
                .id(validUserId)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@email.com")
                .status("ACTIVE")
                .defaultAddressId(null)
                .build();

        UserResponseDTO validUserWithAddress = UserResponseDTO.builder()
                .id(validUserId)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@email.com")
                .status("ACTIVE")
                .defaultAddressId(validAddressId)
                .build();

        // Configurar captura de System.out
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    void getUserById_UserExists_WithDefaultAddress_Success() {
        when(userRepository.findUserById(validUserId))
                .thenReturn(Optional.of(validUserWithoutAddress));
        when(addressRepository.findDefaultAddressIdByUserId(validUserId))
                .thenReturn(Optional.of(validAddressId));

        UserResponseDTO result = userService.getUserById(validUserId);

        assertNotNull(result);
        assertEquals(validUserId, result.getId());
        assertEquals("Juan", result.getFirstName());
        assertEquals("Pérez", result.getLastName());
        assertEquals("juan.perez@email.com", result.getEmail());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(validAddressId, result.getDefaultAddressId());

        verify(userRepository).findUserById(validUserId);
        verify(addressRepository).findDefaultAddressIdByUserId(validUserId);

        String output = outputStream.toString();
        assertTrue(output.contains("User retrieved: Juan Pérez"));
        assertFalse(output.contains("No default address found"));
    }

    @Test
    void getUserById_UserExists_WithoutDefaultAddress_Success() {
        when(userRepository.findUserById(validUserId))
                .thenReturn(Optional.of(validUserWithoutAddress));
        when(addressRepository.findDefaultAddressIdByUserId(validUserId))
                .thenReturn(Optional.empty());

        UserResponseDTO result = userService.getUserById(validUserId);

        assertNotNull(result);
        assertEquals(validUserId, result.getId());
        assertEquals("Juan", result.getFirstName());
        assertEquals("Pérez", result.getLastName());
        assertEquals("juan.perez@email.com", result.getEmail());
        assertEquals("ACTIVE", result.getStatus());
        assertNull(result.getDefaultAddressId());

        verify(userRepository).findUserById(validUserId);
        verify(addressRepository).findDefaultAddressIdByUserId(validUserId);

        String output = outputStream.toString();
        assertTrue(output.contains("User retrieved: Juan Pérez"));
        assertTrue(output.contains("No default address found for user ID: " + validUserId));
        assertTrue(output.contains("Order will be created without shippingAddressId"));
    }

    @Test
    void getUserById_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findUserById(validUserId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(validUserId)
        );

        assertEquals("User not found with ID: " + validUserId, exception.getMessage());

        verify(userRepository).findUserById(validUserId);
        verify(addressRepository, never()).findDefaultAddressIdByUserId(anyLong());

        String output = outputStream.toString();
        assertFalse(output.contains("User retrieved:"));
    }

    @Test
    void getUserById_UserExists_AddressRepositoryThrowsException_UserStillReturned() {
        when(userRepository.findUserById(validUserId))
                .thenReturn(Optional.of(validUserWithoutAddress));
        when(addressRepository.findDefaultAddressIdByUserId(validUserId))
                .thenThrow(new RuntimeException("Database connection error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getUserById(validUserId)
        );

        assertEquals("Database connection error", exception.getMessage());

        verify(userRepository).findUserById(validUserId);
        verify(addressRepository).findDefaultAddressIdByUserId(validUserId);
    }

    @Test
    void getUserById_NullUserId_ThrowsResourceNotFoundException() {
        Long nullUserId = null;
        when(userRepository.findUserById(null))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(null)
        );

        assertEquals("User not found with ID: null", exception.getMessage());

        verify(userRepository).findUserById(null);
        verify(addressRepository, never()).findDefaultAddressIdByUserId(anyLong());
    }

    @Test
    void getUserById_UserWithNullNames_HandlesGracefully() {
        UserResponseDTO userWithNullNames = UserResponseDTO.builder()
                .id(validUserId)
                .firstName(null)
                .lastName(null)
                .email("test@email.com")
                .status("ACTIVE")
                .defaultAddressId(null)
                .build();

        when(userRepository.findUserById(validUserId))
                .thenReturn(Optional.of(userWithNullNames));
        when(addressRepository.findDefaultAddressIdByUserId(validUserId))
                .thenReturn(Optional.empty());

        UserResponseDTO result = userService.getUserById(validUserId);

        assertNotNull(result);
        assertEquals(validUserId, result.getId());
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        assertEquals("test@email.com", result.getEmail());
        assertNull(result.getDefaultAddressId());

        String output = outputStream.toString();
        assertTrue(output.contains("User retrieved: null null"));
        assertTrue(output.contains("No default address found"));
    }

    @Test
    void getUserById_UserExists_AddressFound_VerifySetDefaultAddressIdCalled() {
        UserResponseDTO userSpy = spy(validUserWithoutAddress);
        when(userRepository.findUserById(validUserId))
                .thenReturn(Optional.of(userSpy));
        when(addressRepository.findDefaultAddressIdByUserId(validUserId))
                .thenReturn(Optional.of(validAddressId));

        UserResponseDTO result = userService.getUserById(validUserId);

        assertNotNull(result);
        assertEquals(validAddressId, result.getDefaultAddressId());

        verify(userSpy).setDefaultAddressId(validAddressId);
        verify(userRepository).findUserById(validUserId);
        verify(addressRepository).findDefaultAddressIdByUserId(validUserId);
    }

    @Test
    void getUserById_MultipleUsers_DifferentScenarios() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long addressId1 = 100L;

        UserResponseDTO user1 = UserResponseDTO.builder()
                .id(userId1)
                .firstName("Ana")
                .lastName("García")
                .email("ana@email.com")
                .build();

        UserResponseDTO user2 = UserResponseDTO.builder()
                .id(userId2)
                .firstName("Carlos")
                .lastName("López")
                .email("carlos@email.com")
                .build();

        when(userRepository.findUserById(userId1))
                .thenReturn(Optional.of(user1));
        when(userRepository.findUserById(userId2))
                .thenReturn(Optional.of(user2));
        when(addressRepository.findDefaultAddressIdByUserId(userId1))
                .thenReturn(Optional.of(addressId1));
        when(addressRepository.findDefaultAddressIdByUserId(userId2))
                .thenReturn(Optional.empty());

        UserResponseDTO result1 = userService.getUserById(userId1);
        UserResponseDTO result2 = userService.getUserById(userId2);

        assertEquals(addressId1, result1.getDefaultAddressId());
        assertNull(result2.getDefaultAddressId());

        verify(userRepository).findUserById(userId1);
        verify(userRepository).findUserById(userId2);
        verify(addressRepository).findDefaultAddressIdByUserId(userId1);
        verify(addressRepository).findDefaultAddressIdByUserId(userId2);
    }

}