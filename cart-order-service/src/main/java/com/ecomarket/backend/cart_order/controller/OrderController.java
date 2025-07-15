package com.ecomarket.backend.cart_order.controller;

import com.ecomarket.backend.cart_order.DTO.request.OrderRequestDTO;
import com.ecomarket.backend.cart_order.DTO.response.OrderResponseDTO;
import com.ecomarket.backend.cart_order.DTO.request.OrderStatusUpdateRequestDTO;
import com.ecomarket.backend.cart_order.assembler.OrderAssembler;
import com.ecomarket.backend.cart_order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderAssembler orderAssembler;


    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<OrderResponseDTO>> getOrderById(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(orderAssembler.toModel(order));
    }

    @PostMapping
    public ResponseEntity<EntityModel<OrderResponseDTO>> createOrder(@Valid @RequestBody OrderRequestDTO orderRequest) {
        OrderResponseDTO newOrder = orderService.createOrder(orderRequest);
        return new ResponseEntity<>(orderAssembler.toModel(newOrder), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<OrderResponseDTO>>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<EntityModel<OrderResponseDTO>> orderModels = orders.stream()
                .map(orderAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<OrderResponseDTO>> collectionModel = CollectionModel.of(orderModels,
                linkTo(methodOn(OrderController.class).getAllOrders()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/date")
    public ResponseEntity<CollectionModel<EntityModel<OrderResponseDTO>>> getOrdersByDate(@RequestParam("orderDate") LocalDate orderDate) {
        List<OrderResponseDTO> orders = orderService.getOrdersByDate(orderDate);

        if (orders.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<EntityModel<OrderResponseDTO>> orderModels = orders.stream()
                .map(orderAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<OrderResponseDTO>> collectionModel = CollectionModel.of(orderModels,
                linkTo(methodOn(OrderController.class).getOrdersByDate(orderDate)).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<EntityModel<OrderResponseDTO>> updateOrderStatus(@PathVariable Long id,
                                                                           @RequestBody OrderStatusUpdateRequestDTO request) {
        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(id, request.getNewStatus());
        return ResponseEntity.ok(orderAssembler.toModel(updatedOrder));
    }

}