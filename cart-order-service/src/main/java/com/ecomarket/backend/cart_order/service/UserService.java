package com.ecomarket.backend.cart_order.service;

import com.ecomarket.backend.cart_order.DTO.UserResponseDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserResponseDTO getUserById(Long userId) {
        String userSql = "SELECT id, first_name, last_name, email, status FROM users WHERE id = ?";
        UserResponseDTO user = null;
        try {
            user = jdbcTemplate.queryForObject(userSql, new Object[]{userId}, (rs, rowNum) -> {
                UserResponseDTO u = new UserResponseDTO();
                u.setId(rs.getLong("id"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setStatus(rs.getString("status"));
                return u;
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            System.err.println("Error accessing user data via JDBC: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve user information from database.", e);
        }

        if (user != null) {
            String addressSql = "SELECT id FROM addresses WHERE user_id = ? LIMIT 1";
            try {
                Long defaultAddressId = jdbcTemplate.queryForObject(addressSql, new Object[]{userId}, Long.class);
                user.setDefaultAddressId(defaultAddressId);
            } catch (EmptyResultDataAccessException e) {
                user.setDefaultAddressId(null);
                System.out.println("No address found for user ID: " + userId + ". Order will be created without shippingAddressId.");
            } catch (DataAccessException e) {
                System.err.println("Error accessing address data via JDBC: " + e.getMessage());
                throw new RuntimeException("Failed to retrieve default address for user " + userId, e);
            }
        }
        return user;
    }
}
