package com.ecomarket.backend.cart_order.repository;

import com.ecomarket.backend.cart_order.DTO.response.UserResponseDTO;

import java.util.Optional;

public interface UserRepository {
   Optional<UserResponseDTO> findUserById(Long userId);
}
