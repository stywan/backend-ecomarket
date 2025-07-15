package com.ecomarket.backend.cart_order.DTO.response;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false) // Importante cuando se extiende RepresentationModel
public class OrderResponseDTO extends RepresentationModel<OrderResponseDTO> {
    private Long id;
    private Long userId;
    private Long shippingAddressId;
    private Long paymentTransactionId;
    private LocalDateTime createdAt;
    private String orderStatus;
    private BigDecimal totalAmount;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private List<OrderItemResponseDTO> items;
}
