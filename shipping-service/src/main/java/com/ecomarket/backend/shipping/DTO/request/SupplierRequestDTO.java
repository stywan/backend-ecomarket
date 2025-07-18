package com.ecomarket.backend.shipping.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequestDTO {

    @NotBlank(message = "Supplier name cannot be empty.")
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String contactPerson;

    @Size(max = 20)
    private String phone;

    @Email
    @Size(max = 255)
    private String email;
}
