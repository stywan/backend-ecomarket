package com.ecomarket.backend.cart_order.service;

import com.ecomarket.backend.cart_order.client.ProductClient;
import com.ecomarket.backend.cart_order.exception.ResourceNotFoundException;
import com.ecomarket.backend.cart_order.model.Cart;
import com.ecomarket.backend.cart_order.model.CartItem;
import com.ecomarket.backend.cart_order.model.Order;
import com.ecomarket.backend.cart_order.model.OrderItem;
import com.ecomarket.backend.cart_order.repository.CartRepository;
import com.ecomarket.backend.cart_order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductClient productClient;

    public Order checkout(Long userId, Long shippingAddressId) {

        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));

        for (CartItem item : cart.getItems()) {
            productClient.getProduct(item.getProductId());
        }

        double subtotal = cart.getItems().stream()
                .mapToDouble(i -> i.getQuantity() * i.getUnitPrice())
                .sum();

        double shippingCost = 5.0;
        double totalAmount = subtotal + shippingCost;

        Order order = Order.builder()
                .userId(userId)
                .shippingAddressId(shippingAddressId)
                .paymentTransactionId(null)
                .taxProfileId(null)
                .orderDate(LocalDateTime.now())
                .orderStatus(Order.OrderStatus.PENDING_PAYMENT)
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .totalAmount(totalAmount)
                .build();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(cartItem.getProductId())
                    .unitSalePrice(cartItem.getUnitPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
            order.getItems().add(orderItem);
        }

        cart.setStatus(Cart.Status.CHECKED_OUT);
        cartRepository.save(cart);

        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setOrderStatus(newStatus);

        return orderRepository.save(order);
    }

    public List<Order> listOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
