package com.ecomarket.backend.cart_order.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    private Long userId;

    private LocalDateTime creationDate;

    private LocalDateTime lastUpdate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items;

    public enum Status {
        ACTIVE, COMPLETED, ABANDONED
    }
}