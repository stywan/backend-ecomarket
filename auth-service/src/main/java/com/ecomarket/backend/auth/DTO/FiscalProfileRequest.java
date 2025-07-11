package com.ecomarket.backend.auth.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalProfileRequest {

    @NotBlank
    private String rut;

    @NotBlank
    private String businessName;

    private String businessField;

    private String fiscalAddress;

    private String fiscalCommune;

    private String fiscalCity;
}