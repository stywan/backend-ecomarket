package com.ecomarket.backend.cart_order.repository;

import java.util.Optional;

public interface AddressRepository {
    Optional<Long> findDefaultAddressIdByUserId(Long userId);
}
