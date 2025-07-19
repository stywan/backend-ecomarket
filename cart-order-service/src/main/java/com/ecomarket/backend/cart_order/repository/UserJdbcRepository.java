package com.ecomarket.backend.cart_order.repository;

import com.ecomarket.backend.cart_order.DTO.response.UserResponseDTO;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Etiqueta esta clase como un componente de Spring para acceso a datos
public class UserJdbcRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserResponseDTO> findUserById(Long userId) {
        // Consulta SQL para obtener el usuario
        String userSql = "SELECT id, first_name, last_name, email, status FROM users WHERE id = ?";

        try {
            // queryForObject espera un resultado, si no lo encuentra, lanza EmptyResultDataAccessException
            UserResponseDTO user = jdbcTemplate.queryForObject(userSql, new Object[]{userId}, (rs, rowNum) -> {
                UserResponseDTO u = new UserResponseDTO();
                u.setId(rs.getLong("id"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setStatus(rs.getString("status"));return u;
            });
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}