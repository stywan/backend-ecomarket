package com.ecomarket.backend.shipping.DTO.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDTO {
    private Integer supplierId;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
}
