package com.ecomarket.backend.cart_order.client.dto;
import lombok.Data;

@Data
public class AddressDto {
    private Long id;
    private Long userId;
    private String street;
    private String number;
    private String commune;
    private String postalCode;
}