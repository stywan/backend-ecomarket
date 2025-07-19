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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private UserService userService;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @InjectMocks
    private OrderService orderService;

    private OrderRequestDTO validOrderRequest;
    private UserResponseDTO validUser;
    private ProductResponseDTO validProduct;
    private InventoryResponseDTO validInventory;
    private TransactionResponseDTO validTransaction;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        // Setup valid test data
        validOrderRequest = OrderRequestDTO.builder()
                .userId(1L)
                .items(Arrays.asList(
                        OrderItemRequestDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .build()
                ))
                .build();

        validUser = UserResponseDTO.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .defaultAddressId(1L)
                .build();

        validProduct = ProductResponseDTO.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("10000.00"))
                .build();

        validInventory = InventoryResponseDTO.builder()
                .id(1L)
                .productId(1L)
                .availableQuantity(10)
                .build();

        validTransaction = TransactionResponseDTO.builder()
                .transactionId(1L)
                .orderId(1L)
                .userId(1L)
                .amount(new BigDecimal("23990.00"))
                .currency("CLP")
                .paymentMethod("Credit Card")
                .transactionStatus("PENDING")
                .build();

        savedOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .shippingAddressId(1L)
                .orderStatus(Order.OrderStatus.PENDING_PAYMENT)
                .subtotal(new BigDecimal("20000.00"))
                .totalAmount(new BigDecimal("23990.00"))
                .shippingCost(new BigDecimal("3990.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrder_Success() {
        // Given
        when(userService.getUserById(1L)).thenReturn(validUser);
        when(productServiceClient.getProductById(1L)).thenReturn(validProduct);
        when(productServiceClient.getProductInventory(1L)).thenReturn(validInventory);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(paymentServiceClient.createPaymentTransaction(any(TransactionRequestDTO.class)))
                .thenReturn(validTransaction);
        when(orderItemRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // When
        OrderResponseDTO result = orderService.createOrder(validOrderRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals("PENDING_PAYMENT", result.getOrderStatus());
        assertEquals(new BigDecimal("23990.00"), result.getTotalAmount());

        // Verify interactions
        verify(userService).getUserById(1L);
        verify(productServiceClient).getProductById(1L);
        verify(productServiceClient).getProductInventory(1L);
        verify(productServiceClient).performInventoryOperation(1L, "RESERVE", 2);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(paymentServiceClient).createPaymentTransaction(any(TransactionRequestDTO.class));
    }

    @Test
    void createOrder_NullUserId_ThrowsException() {
        // Given
        OrderRequestDTO invalidRequest = OrderRequestDTO.builder()
                .userId(null)
                .items(Arrays.asList(OrderItemRequestDTO.builder().productId(1L).quantity(1).build()))
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(invalidRequest)
        );

        assertEquals("User ID is required to create an order.", exception.getMessage());
        verifyNoInteractions(userService, productServiceClient, orderRepository);
    }

    @Test
    void createOrder_EmptyItems_ThrowsException() {
        // Given
        OrderRequestDTO invalidRequest = OrderRequestDTO.builder()
                .userId(1L)
                .items(Arrays.asList())
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(invalidRequest)
        );

        assertEquals("Order must contain at least one item.", exception.getMessage());
        verifyNoInteractions(userService, productServiceClient, orderRepository);
    }

    @Test
    void createOrder_UserNotFound_ThrowsException() {
        // Given
        when(userService.getUserById(1L)).thenThrow(new DataAccessException("User not found") {});

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(validOrderRequest)
        );

        assertEquals("Failed to retrieve user information.", exception.getMessage());
        verify(userService).getUserById(1L);
        verifyNoInteractions(productServiceClient, orderRepository);
    }

    @Test
    void createOrder_UserWithoutDefaultAddress_ThrowsException() {
        // Given
        UserResponseDTO userWithoutAddress = UserResponseDTO.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .defaultAddressId(null)
                .build();

        when(userService.getUserById(1L)).thenReturn(userWithoutAddress);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(validOrderRequest)
        );

        assertTrue(exception.getMessage().contains("does not have a default shipping address"));
        verify(userService).getUserById(1L);
        verifyNoInteractions(productServiceClient, orderRepository);
    }


    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        // Given
        InventoryResponseDTO insufficientInventory = InventoryResponseDTO.builder()
                .id(1L)
                .productId(1L)
                .availableQuantity(1) // Less than requested quantity (2)
                .build();

        when(userService.getUserById(1L)).thenReturn(validUser);
        when(productServiceClient.getProductById(1L)).thenReturn(validProduct);
        when(productServiceClient.getProductInventory(1L)).thenReturn(insufficientInventory);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(validOrderRequest)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(userService).getUserById(1L);
        verify(productServiceClient).getProductById(1L);
        verify(productServiceClient).getProductInventory(1L);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void createOrder_PaymentServiceFailure_ThrowsException() {
        // Given
        when(userService.getUserById(1L)).thenReturn(validUser);
        when(productServiceClient.getProductById(1L)).thenReturn(validProduct);
        when(productServiceClient.getProductInventory(1L)).thenReturn(validInventory);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(paymentServiceClient.createPaymentTransaction(any(TransactionRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Payment failed"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(validOrderRequest)
        );

        assertEquals("Payment transaction failed: Payment failed", exception.getMessage());
        verify(paymentServiceClient).createPaymentTransaction(any(TransactionRequestDTO.class));
    }

    @Test
    void updateOrderStatus_Success() {
        // Given
        Order existingOrder = Order.builder()
                .id(1L)
                .orderStatus(Order.OrderStatus.PENDING_PAYMENT)
                .items(Arrays.asList())
                .build();

        Order updatedOrder = Order.builder()
                .id(1L)
                .orderStatus(Order.OrderStatus.CONFIRMED)
                .items(Arrays.asList())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        // When
        OrderResponseDTO result = orderService.updateOrderStatus(1L, "CONFIRMED");

        // Then
        assertNotNull(result);
        assertEquals("CONFIRMED", result.getOrderStatus());
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_OrderNotFound_ThrowsException() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(1L, "CONFIRMED")
        );

        assertEquals("Order not found with ID: 1", exception.getMessage());
        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_InvalidStatus_ThrowsException() {
        // Given
        Order existingOrder = Order.builder()
                .id(1L)
                .orderStatus(Order.OrderStatus.PENDING_PAYMENT)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(1L, "INVALID_STATUS")
        );

        assertTrue(exception.getMessage().contains("Invalid order status"));
        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_CancelledOrder_ReleasesStock() {
        // Given
        OrderItem orderItem = OrderItem.builder()
                .productId(1L)
                .quantity(2)
                .build();

        Order existingOrder = Order.builder()
                .id(1L)
                .orderStatus(Order.OrderStatus.CONFIRMED)
                .items(Arrays.asList(orderItem))
                .build();

        Order cancelledOrder = Order.builder()
                .id(1L)
                .orderStatus(Order.OrderStatus.CANCELLED)
                .items(Arrays.asList(orderItem))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

        // When
        OrderResponseDTO result = orderService.updateOrderStatus(1L, "CANCELLED");

        // Then
        assertNotNull(result);
        assertEquals("CANCELLED", result.getOrderStatus());
        verify(productServiceClient).performInventoryOperation(1L, "RELEASE", 2);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getOrderById_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));

        // When
        OrderResponseDTO result = orderService.getOrderById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_NotFound_ReturnsNull() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        OrderResponseDTO result = orderService.getOrderById(1L);

        // Then
        assertNull(result);
        verify(orderRepository).findById(1L);
    }

    @Test
    void getAllOrders_Success() {
        // Given
        List<Order> orders = Arrays.asList(savedOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<OrderResponseDTO> result = orderService.getAllOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(orderRepository).findAll();
    }
}