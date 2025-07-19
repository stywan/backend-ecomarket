package com.ecomarket.backend.auth.service;

import com.ecomarket.backend.auth.DTO.PasswordUpdateRequest;
import com.ecomarket.backend.auth.DTO.UserResponse;
import com.ecomarket.backend.auth.DTO.UserUpdateRequest;
import com.ecomarket.backend.auth.model.Role;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserUpdateRequest userUpdateRequest;
    private PasswordUpdateRequest passwordUpdateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .passwordHash("encodedPassword")
                .role(Role.builder().roleName("ROLE_USER").build())
                .status("ACTIVE") // Changed from enum UserStatus.ACTIVE to String "ACTIVE"
                .createdAt(LocalDateTime.now().minusDays(5))
                .lastLogin(LocalDateTime.now().minusDays(1))
                .build();

        userUpdateRequest = UserUpdateRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

        passwordUpdateRequest = PasswordUpdateRequest.builder()
                .currentPassword("oldPassword")
                .newPassword("newPassword123")
                .build();
    }

    @Test
    void updateProfile_shouldUpdateAndSaveUser() {
        userService.updateProfile(testUser, userUpdateRequest);

        assertEquals(userUpdateRequest.getFirstName(), testUser.getFirstName());
        assertEquals(userUpdateRequest.getLastName(), testUser.getLastName());
        assertEquals(userUpdateRequest.getEmail(), testUser.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updatePassword_shouldUpdateAndSavePassword_whenCurrentPasswordMatches() {
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        userService.updatePassword(testUser, passwordUpdateRequest);

        assertEquals("newEncodedPassword", testUser.getPasswordHash());
        verify(passwordEncoder, times(1)).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updatePassword_shouldThrowException_whenCurrentPasswordDoesNotMatch() {
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.updatePassword(testUser, passwordUpdateRequest));

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(passwordEncoder, times(1)).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByEmail_shouldReturnUser_whenUserExists() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserByEmail("john.doe@example.com");

        assertNotNull(foundUser);
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
    }

    @Test
    void getUserByEmail_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.getUserByEmail("nonexistent@example.com"));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void getUserProfile_shouldReturnUserResponse_whenGivenUser() {
        UserResponse response = userService.getUserProfile(testUser);

        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getFirstName(), response.getFirstName());
        assertEquals(testUser.getLastName(), response.getLastName());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getRole().getRoleName(), response.getRole());
        assertEquals(testUser.getStatus(), response.getStatus()); // Asserting String status directly
        assertEquals(testUser.getCreatedAt(), response.getCreatedAt());
        assertEquals(testUser.getLastLogin(), response.getLastLogin());
    }
}