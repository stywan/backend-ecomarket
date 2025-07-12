package com.ecomarket.backend.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fiscal_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String rut;

    @NotBlank
    @Column(nullable = false)
    private String businessName;

    private String businessField;

    private String fiscalAddress;

    private String fiscalCommune;

    private String fiscalCity;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
