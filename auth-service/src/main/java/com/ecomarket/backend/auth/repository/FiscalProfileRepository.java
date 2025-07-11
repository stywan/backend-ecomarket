package com.ecomarket.backend.auth.repository;

import com.ecomarket.backend.auth.model.FiscalProfile;
import com.ecomarket.backend.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FiscalProfileRepository extends JpaRepository<FiscalProfile, Long> {
    List<FiscalProfile> findByUser(User user);
}
