package com.ecomarket.backend.cart_order.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AddressJdbcRepository implements AddressRepository {

    private final JdbcTemplate jdbcTemplate;

    public AddressJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Long> findDefaultAddressIdByUserId(Long userId) {
        String addressSql = "SELECT id FROM addresses WHERE user_id = ? LIMIT 1";
        try {
            Long defaultAddressId = jdbcTemplate.queryForObject(addressSql, new Object[]{userId}, Long.class);
            return Optional.ofNullable(defaultAddressId);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
