package com.ecomarket.backend.auth.DTO;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank
    private String street;

    @NotBlank
    private String number;

    @NotBlank
    private String commune;

    @NotBlank
    private String postalCode;
}
