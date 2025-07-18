package com.ecomarket.backend.payment.DTO.request;

import lombok.*;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptRequestDTO {

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "Transaction ID cannot be null")
    private Long transactionId;

    @NotBlank(message = "Document number cannot be blank")
    @Size(max = 50, message = "Document number cannot exceed 50 characters")
    private String documentNumber;

    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    @NotNull(message = "Taxes cannot be null")
    @DecimalMin(value = "0.0", message = "Taxes cannot be negative")
    private BigDecimal taxes;

    @NotBlank(message = "Customer RUT cannot be blank")
    @Pattern(regexp = "^[0-9]{7,8}-[0-9Kk]$", message = "Customer RUT must be in format XXXXXXXX-X or XXXXXXX-X")
    private String customerRut;

    @NotBlank(message = "Customer name cannot be blank")
    @Size(max = 100, message = "Customer name cannot exceed 100 characters")
    private String customerName;
}
