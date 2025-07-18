package com.ecomarket.backend.payment.DTO.request;

import lombok.*;

import java.math.BigDecimal;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequestDTO {

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

    @NotNull(message = "Tax Profile ID cannot be null")
    private Long taxProfileId;
}
