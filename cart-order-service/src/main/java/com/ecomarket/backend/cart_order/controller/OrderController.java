package com.ecomarket.backend.cart_order.controller;

import com.ecomarket.backend.cart_order.DTO.OrderRequestDTO;
import com.ecomarket.backend.cart_order.DTO.OrderResponseDTO;
import com.ecomarket.backend.cart_order.DTO.OrderStatusUpdateRequestDTO;
import com.ecomarket.backend.cart_order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// OrderController.java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO orderRequest) {
        try {
            OrderResponseDTO newOrder = orderService.createOrder(orderRequest);
            return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/date")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByDate(@RequestParam("orderDate") LocalDate orderDate) {
        try {
            List<OrderResponseDTO> orders = orderService.getOrdersByDate(orderDate);
            if (orders.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching orders by date: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long id,
                                                              @RequestBody OrderStatusUpdateRequestDTO request) {
        try {
            OrderResponseDTO updatedOrder = orderService.updateOrderStatus(id, request.getNewStatus());
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK); // 200 OK
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}