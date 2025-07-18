package com.ecomarket.backend.shipping.DTO.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDTO extends RepresentationModel<SupplierResponseDTO> {
    private Integer supplierId;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
}
