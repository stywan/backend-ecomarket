package com.ecomarket.backend.payment.DTO.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptResponseDTO {
    private Long receiptId;
    private Long orderId;
    private Long transactionId;
    private String documentNumber;
    private LocalDateTime issueDate;
    private BigDecimal totalAmount;
    private BigDecimal taxes;
    private String customerRut;
    private String customerName;
    private String status;
}
