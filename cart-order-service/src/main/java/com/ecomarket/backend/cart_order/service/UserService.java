package com.ecomarket.backend.cart_order.service;

import com.ecomarket.backend.cart_order.DTO.response.UserResponseDTO;
import com.ecomarket.backend.cart_order.exception.ResourceNotFoundException;
import com.ecomarket.backend.cart_order.repository.AddressRepository;
import com.ecomarket.backend.cart_order.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserResponseDTO getUserById(Long userId) {
        UserResponseDTO userResponseDTO = userRepository.findUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        addressRepository.findDefaultAddressIdByUserId(userId)
                .ifPresent(userResponseDTO::setDefaultAddressId);

        System.out.println("User retrieved: " + userResponseDTO.getFirstName() + " " + userResponseDTO.getLastName());
        if (userResponseDTO.getDefaultAddressId() == null) {
            System.out.println("No default address found for user ID: " + userId + ". Order will be created without shippingAddressId.");
        }

        return userResponseDTO;
    }
}
