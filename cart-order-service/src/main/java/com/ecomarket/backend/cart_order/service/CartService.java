package com.ecomarket.backend.cart_order.service;

import com.ecomarket.backend.cart_order.client.AuthClient;
import com.ecomarket.backend.cart_order.client.ProductClient;
import com.ecomarket.backend.cart_order.client.dto.ProductDto;
import com.ecomarket.backend.cart_order.client.dto.UserDto;
import com.ecomarket.backend.cart_order.exception.ResourceNotFoundException;
import com.ecomarket.backend.cart_order.model.Cart;
import com.ecomarket.backend.cart_order.model.CartItem;
import com.ecomarket.backend.cart_order.repository.CartItemRepository;
import com.ecomarket.backend.cart_order.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AuthClient authClient;
    private final ProductClient productClient;

    public Cart addItemToCart(Long userId, Long productId, Integer quantity) {

        UserDto user = authClient.getUser(userId);
        ProductDto product = productClient.getProduct(productId);

        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.Status.ACTIVE)
                .orElseGet(() -> Cart.builder()
                        .userId(userId)
                        .status(Cart.Status.ACTIVE)
                        .build());

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = CartItem.builder()
                    .cart(cart)
                    .productId(productId)
                    .unitPrice(product.getPrice())
                    .quantity(quantity)
                    .build();
            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        item.setQuantity(quantity);

        cartRepository.save(cart);
        return cart;
    }

    public Cart removeItemFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getItems().removeIf(i -> i.getProductId().equals(productId));

        cartRepository.save(cart);
        return cart;
    }

    public Cart getCartByUser(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, Cart.Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }
}
