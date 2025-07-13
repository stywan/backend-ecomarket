package com.ecomarket.backend.cart_order.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    private LocalDateTime requestDate;

    private String reason;

    @Enumerated(EnumType.STRING)
    private ReturnStatus returnStatus;

    private Double refundAmount;

    private LocalDateTime refundDate;

    public enum ReturnStatus {
        REQUESTED, APPROVED, REJECTED, COMPLETED
    }
}