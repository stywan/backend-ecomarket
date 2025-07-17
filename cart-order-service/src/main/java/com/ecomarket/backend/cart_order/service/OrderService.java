package com.ecomarket.backend.cart_order.service;

import com.ecomarket.backend.cart_order.DTO.request.OrderItemRequestDTO;
import com.ecomarket.backend.cart_order.DTO.request.OrderRequestDTO;
import com.ecomarket.backend.cart_order.DTO.request.TransactionRequestDTO;
import com.ecomarket.backend.cart_order.DTO.response.*;
import com.ecomarket.backend.cart_order.client.PaymentServiceClient;
import com.ecomarket.backend.cart_order.client.ProductServiceClient;
import com.ecomarket.backend.cart_order.model.Order;
import com.ecomarket.backend.cart_order.model.OrderItem;
import com.ecomarket.backend.cart_order.repository.OrderItemRepository;
import com.ecomarket.backend.cart_order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductServiceClient productServiceClient;
    private final UserService userService;
    private final PaymentServiceClient paymentServiceClient;

    private static final BigDecimal FIXED_SHIPPING_COST = new BigDecimal("3990.00");
    private static final String DEFAULT_CURRENCY = "CLP";
    private static final String DEFAULT_PAYMENT_METHOD = "Credit Card";

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequest) {
        validateOrderRequest(orderRequest);

        UserResponseDTO user = getUserForOrder(orderRequest.getUserId());
        validateUserForOrder(user);

        Order order = createInitialOrder(user);

        List<OrderItem> orderItems = processOrderItems(orderRequest.getItems());
        OrderTotals totals = calculateOrderTotals(orderItems);

        Order savedOrder = saveOrderWithItemsAndTotals(order, orderItems, totals);

        TransactionResponseDTO paymentResponse = createPaymentTransaction(savedOrder);
        updateOrderWithPaymentTransaction(savedOrder, paymentResponse);

        return convertToOrderResponseDTO(savedOrder);
    }

    // --- Métodos de validación ---

    private void validateOrderRequest(OrderRequestDTO orderRequest) {
        if (orderRequest.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required to create an order.");
        }

        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }
    }

    private UserResponseDTO getUserForOrder(Long userId) {
        try {
            return userService.getUserById(userId);
        } catch (DataAccessException e) {
            System.err.println("Error accessing user data: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve user information.", e);
        }
    }

    private void validateUserForOrder(UserResponseDTO user) {
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + user.getId());
        }

        if (user.getDefaultAddressId() == null) {
            throw new IllegalArgumentException("User " + user.getId() + " does not have a default shipping address assigned. Order cannot be created.");
        }
    }

    // --- Métodos de creación de Order ---

    private Order createInitialOrder(UserResponseDTO user) {
        return Order.builder()
                .userId(user.getId())
                .shippingAddressId(user.getDefaultAddressId())
                .paymentTransactionId(null) // Se establecerá después del pago
                .createdAt(LocalDateTime.now())
                .orderStatus(Order.OrderStatus.PENDING_PAYMENT)
                .shippingCost(FIXED_SHIPPING_COST)
                .totalAmount(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .build();
    }

    // --- Métodos de procesamiento de items ---

    private List<OrderItem> processOrderItems(List<OrderItemRequestDTO> itemRequests) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDTO itemRequest : itemRequests) {
            ProductResponseDTO product = getProductForOrder(itemRequest.getProductId());
            validateProductForOrder(product, itemRequest);

            validateAndReserveStock(product, itemRequest);

            OrderItem orderItem = createOrderItem(product, itemRequest);
            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private ProductResponseDTO getProductForOrder(Long productId) {
        try {
            return productServiceClient.getProductById(productId);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        } catch (Exception ex) {
            System.err.println("Error retrieving product ID " + productId + ": " + ex.getMessage());
            throw new RuntimeException("Failed to retrieve product details for ID: " + productId, ex);
        }
    }

    private void validateProductForOrder(ProductResponseDTO product, OrderItemRequestDTO itemRequest) {
        if (product == null) {
            throw new IllegalArgumentException("Product details could not be retrieved for ID: " + itemRequest.getProductId());
        }

        if (product.getPrice() == null) {
            throw new IllegalArgumentException("Product price is not available for product: " + product.getName());
        }

        if (itemRequest.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity for product " + product.getName() + " must be greater than zero.");
        }
    }

    // --- Métodos de manejo de inventario ---

    private void validateAndReserveStock(ProductResponseDTO product, OrderItemRequestDTO itemRequest) {
        InventoryResponseDTO inventory = getProductInventory(product.getId());
        validateStockAvailability(inventory, product, itemRequest);
        reserveProductStock(product, itemRequest);
    }

    private InventoryResponseDTO getProductInventory(Long productId) {
        InventoryResponseDTO inventory = productServiceClient.getProductInventory(productId);
        if (inventory == null) {
            throw new IllegalArgumentException("Inventory information not found for product ID: " + productId);
        }
        return inventory;
    }

    private void validateStockAvailability(InventoryResponseDTO inventory, ProductResponseDTO product, OrderItemRequestDTO itemRequest) {
        if (inventory.getAvailableQuantity() < itemRequest.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock for product '" + product.getName() + "'. Available: " + inventory.getAvailableQuantity() + ", Requested: " + itemRequest.getQuantity());
        }
    }

    private void reserveProductStock(ProductResponseDTO product, OrderItemRequestDTO itemRequest) {
        try {
            productServiceClient.performInventoryOperation(
                    itemRequest.getProductId(),
                    "RESERVE",
                    itemRequest.getQuantity()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to reserve stock for product '" + product.getName() + "': " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error reserving stock for product " + product.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to reserve stock for product: " + product.getName(), e);
        }
    }

    // --- Métodos de creación de OrderItem ---

    private OrderItem createOrderItem(ProductResponseDTO product, OrderItemRequestDTO itemRequest) {
        BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

        return OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productPrice(product.getPrice())
                .quantity(itemRequest.getQuantity())
                .subtotal(itemSubtotal)
                .build();
    }

    // --- Métodos de cálculo de totales ---

    private OrderTotals calculateOrderTotals(List<OrderItem> orderItems) {
        BigDecimal totalSubtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = totalSubtotal.add(FIXED_SHIPPING_COST);

        return new OrderTotals(totalSubtotal, totalAmount);
    }

    // --- Métodos de persistencia ---

    private Order saveOrderWithItemsAndTotals(Order order, List<OrderItem> orderItems, OrderTotals totals) {
        updateOrderTotals(order, totals);
        Order savedOrder = orderRepository.save(order);

        assignOrderToItems(savedOrder, orderItems);
        orderItemRepository.saveAll(orderItems);

        savedOrder.setItems(orderItems);
        return savedOrder;
    }

    private void updateOrderTotals(Order order, OrderTotals totals) {
        order.setSubtotal(totals.subtotal);
        order.setTotalAmount(totals.totalAmount);
    }

    private void assignOrderToItems(Order savedOrder, List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }
    }

    // --- Métodos de manejo de pagos ---

    private TransactionResponseDTO createPaymentTransaction(Order savedOrder) {
        TransactionRequestDTO paymentRequest = buildPaymentRequest(savedOrder);

        try {
            return paymentServiceClient.createPaymentTransaction(paymentRequest);
        } catch (IllegalArgumentException e) {
            System.err.println("Payment transaction failed for order " + savedOrder.getId() + ": " + e.getMessage());
            throw new RuntimeException("Payment transaction failed: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Error calling Payment Service for order " + savedOrder.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to process payment transaction.", e);
        }
    }

    private TransactionRequestDTO buildPaymentRequest(Order savedOrder) {
        return TransactionRequestDTO.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .amount(savedOrder.getTotalAmount())
                .currency(DEFAULT_CURRENCY)
                .paymentMethod(DEFAULT_PAYMENT_METHOD)
                .build();
    }

    private void updateOrderWithPaymentTransaction(Order savedOrder, TransactionResponseDTO paymentResponse) {
        savedOrder.setPaymentTransactionId(paymentResponse.getTransactionId());
        orderRepository.save(savedOrder);
    }

    // --- Métodos de consulta ---

    @Transactional
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<OrderResponseDTO> getOrdersByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return orderRepository.findByCreatedAtBetween(startOfDay, endOfDay).stream()
                .map(this::convertToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    // --- Métodos de actualización de estado ---

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, String newStatus) {
        Order order = findOrderById(orderId);
        Order.OrderStatus oldStatus = order.getOrderStatus();
        Order.OrderStatus statusToUpdate = validateAndParseOrderStatus(newStatus);

        order.setOrderStatus(statusToUpdate);
        Order updatedOrder = orderRepository.save(order);

        handleStockCompensationIfCancelled(oldStatus, statusToUpdate, updatedOrder);

        return convertToOrderResponseDTO(updatedOrder);
    }

    private Order findOrderById(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        return optionalOrder.get();
    }

    private Order.OrderStatus validateAndParseOrderStatus(String newStatus) {
        try {
            return Order.OrderStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + newStatus + ". Valid statuses are: " +
                    java.util.Arrays.stream(Order.OrderStatus.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")));
        }
    }

    private void handleStockCompensationIfCancelled(Order.OrderStatus oldStatus, Order.OrderStatus newStatus, Order order) {
        if (oldStatus != Order.OrderStatus.CANCELLED && newStatus == Order.OrderStatus.CANCELLED) {
            releaseStockForCancelledOrder(order);
        }
    }

    private void releaseStockForCancelledOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            try {
                productServiceClient.performInventoryOperation(
                        item.getProductId(),
                        "RELEASE",
                        item.getQuantity()
                );
            } catch (Exception e) {
                System.err.println("Failed to release stock for product " + item.getProductId() + " on order cancellation: " + e.getMessage());
            }
        }
    }

    // --- Métodos de conversión ---

    private OrderResponseDTO convertToOrderResponseDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = convertOrderItemsToDTO(order.getItems());

        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .shippingAddressId(order.getShippingAddressId())
                .paymentTransactionId(order.getPaymentTransactionId())
                .createdAt(order.getCreatedAt())
                .orderStatus(order.getOrderStatus().name())
                .totalAmount(order.getTotalAmount())
                .subtotal(order.getSubtotal())
                .shippingCost(order.getShippingCost())
                .items(itemDTOs)
                .build();
    }

    private List<OrderItemResponseDTO> convertOrderItemsToDTO(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return new ArrayList<>();
        }

        return orderItems.stream()
                .map(item -> OrderItemResponseDTO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productPrice(item.getProductPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());
    }

    // --- Clases internas para organizar datos ---

    @Transactional
    public OrderResponseDTO getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToOrderResponseDTO)
                .orElse(null);
    }

    private static class OrderTotals {
        private final BigDecimal subtotal;
        private final BigDecimal totalAmount;

        public OrderTotals(BigDecimal subtotal, BigDecimal totalAmount) {
            this.subtotal = subtotal;
            this.totalAmount = totalAmount;
        }
    }
}
