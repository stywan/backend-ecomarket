package com.ecomarket.backend.auth.repository;

import com.ecomarket.backend.auth.model.Address;
import com.ecomarket.backend.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
}
