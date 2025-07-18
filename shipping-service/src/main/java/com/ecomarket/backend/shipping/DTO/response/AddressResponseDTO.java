package com.ecomarket.backend.shipping.DTO.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDTO {
    private Long id;
    private String street;
    private String number;
    private String commune;
    private String postalCode;
}
