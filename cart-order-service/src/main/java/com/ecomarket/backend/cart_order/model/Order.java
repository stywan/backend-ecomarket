package com.ecomarket.backend.cart_order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // CONEXIÓN CON USUARIO

    private Long shippingAddressId; // CONEXIÓN CON DIRECCIÓN DE ENVÍO

    private Long paymentTransactionId; // CONEXIÓN CON TRANSACCIÓN DE PAGO

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private BigDecimal totalAmount;

    private BigDecimal subtotal;

    private BigDecimal shippingCost;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    public enum OrderStatus {
        PENDING_PAYMENT,
        CONFIRMED,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
}