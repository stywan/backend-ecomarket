package com.ecomarket.backend.auth.DTO;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalProfileResponse {
    private Long id;
    private String rut;
    private String businessName;
    private String businessField;
    private String fiscalAddress;
    private String fiscalCommune;
    private String fiscalCity;
}