package com.ecomarket.backend.auth.DTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String street;
    private String number;
    private String commune;
    private String postalCode;
}