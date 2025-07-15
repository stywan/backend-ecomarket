package com.ecomarket.backend.cart_order.service;

import com.ecomarket.backend.cart_order.DTO.*;
import com.ecomarket.backend.cart_order.client.ProductServiceClient;
import com.ecomarket.backend.cart_order.model.Order;
import com.ecomarket.backend.cart_order.model.OrderItem;
import com.ecomarket.backend.cart_order.repository.OrderItemRepository;
import com.ecomarket.backend.cart_order.repository.OrderRepository;
import jakarta.transaction.Transactional;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductServiceClient productServiceClient;
    private final UserService userService; // Servicio para obtener datos de usuario vía JDBC

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductServiceClient productServiceClient,
                        UserService userService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productServiceClient = productServiceClient;
        this.userService = userService;
    }

    @Transactional // Asegura que todas las operaciones de DB sean atómicas
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequest) {
        if (orderRequest.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required to create an order.");
        }
        UserResponseDTO user = null;
        try {
            user = userService.getUserById(orderRequest.getUserId());
        } catch (DataAccessException e) {
            System.err.println("Error accessing user data: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve user information.", e);
        }

        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + orderRequest.getUserId());
        }

        if (user.getDefaultAddressId() == null) {
            throw new IllegalArgumentException("User " + user.getId() + " does not have a default shipping address assigned. Order cannot be created.");
        }


        Order order = Order.builder()
                .userId(user.getId())
                .shippingAddressId(user.getDefaultAddressId())
                .paymentTransactionId(orderRequest.getPaymentTransactionId())
                .createdAt(LocalDateTime.now())
                .orderStatus(Order.OrderStatus.PENDING_PAYMENT)
                .shippingCost(new BigDecimal("3990.00"))
                .totalAmount(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .build();

        Order savedOrder = orderRepository.save(order);

        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        BigDecimal totalOrderSubtotal = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        for (OrderItemRequestDTO itemRequest : orderRequest.getItems()) {
            ProductResponseDTO product;
            try {
                product = productServiceClient.getProductById(itemRequest.getProductId());
            } catch (HttpClientErrorException.NotFound ex) {
                throw new IllegalArgumentException("Product not found with ID: " + itemRequest.getProductId());
            } catch (Exception ex) {
                System.err.println("Error retrieving product ID " + itemRequest.getProductId() + ": " + ex.getMessage());
                throw new RuntimeException("Failed to retrieve product details for ID: " + itemRequest.getProductId(), ex);
            }

            if (product == null) {
                throw new IllegalArgumentException("Product details could not be retrieved for ID: " + itemRequest.getProductId());
            }
            if (product.getPrice() == null) {
                throw new IllegalArgumentException("Product price is not available for product: " + product.getName());
            }
            if (itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity for product " + product.getName() + " must be greater than zero.");
            }

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .subtotal(itemSubtotal)
                    .build();

            orderItems.add(orderItem);
            totalOrderAmount = totalOrderAmount.add(itemSubtotal);
            totalOrderSubtotal = totalOrderSubtotal.add(itemSubtotal);

            // 4. Lógica para actualizar el stock en el servicio de Product
            // Este es un punto crítico. La actualización de stock debe ser manejada con cuidado
            // para evitar condiciones de carrera o inconsistencias.
            // Una llamada directa aquí (`productServiceClient.updateProductStock(...)`)
            // haría que la transacción de la orden dependa del éxito de la llamada de stock.
            // Para una solución más robusta en microservicios, considera un enfoque asíncrono
            // (e.g., con colas de mensajes como Kafka, RabbitMQ) para actualizar el stock.
            // Por ahora, lo dejamos comentado si no tienes un endpoint de stock directo y transaccional.
            // productServiceClient.updateProductStock(product.getId(), -itemRequest.getQuantity());
        }

        orderItemRepository.saveAll(orderItems);
        savedOrder.setItems(orderItems);
        savedOrder.setSubtotal(totalOrderSubtotal);
        savedOrder.setTotalAmount(totalOrderSubtotal.add(savedOrder.getShippingCost()));

        orderRepository.save(savedOrder);

        return convertToOrderResponseDTO(savedOrder);
    }

    // --- Métodos de utilidad ---

    private OrderResponseDTO convertToOrderResponseDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = new ArrayList<>();
        if (order.getItems() != null) {
            itemDTOs = order.getItems().stream()
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

        // Asumiendo que OrderRepository tendrá un método como findByCreatedAtBetween
        return orderRepository.findByCreatedAtBetween(startOfDay, endOfDay).stream()
                .map(this::convertToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, String newStatus) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        Order order = optionalOrder.get();

        Order.OrderStatus statusToUpdate;
        try {
            statusToUpdate = Order.OrderStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + newStatus + ". Valid statuses are: " +
                    java.util.Arrays.stream(Order.OrderStatus.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")));
        }

        order.setOrderStatus(statusToUpdate);
        Order updatedOrder = orderRepository.save(order);
        return convertToOrderResponseDTO(updatedOrder);
    }
}
