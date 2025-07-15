package com.ecomarket.backend.payment.DTO;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDTO {
    private Long invoiceId;
    private Long orderId;
    private Long transactionId;
    private String documentNumber;
    private LocalDateTime issueDate;
    private BigDecimal totalAmount;
    private BigDecimal taxes;
    private Long taxProfileId;
    private String status;
}
