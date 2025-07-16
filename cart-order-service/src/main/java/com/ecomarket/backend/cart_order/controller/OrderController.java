package com.ecomarket.backend.cart_order.controller;

import com.ecomarket.backend.cart_order.DTO.request.OrderRequestDTO;
import com.ecomarket.backend.cart_order.DTO.response.OrderResponseDTO;
import com.ecomarket.backend.cart_order.DTO.request.OrderStatusUpdateRequestDTO;
import com.ecomarket.backend.cart_order.assembler.OrderAssembler;
import com.ecomarket.backend.cart_order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Orders", description = "Gestión de órdenes")
public class OrderController {

    private final OrderService orderService;
    private final OrderAssembler orderAssembler;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden por ID", description = "Devuelve la orden correspondiente al ID proporcionado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Orden encontrada"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<EntityModel<OrderResponseDTO>> getOrderById(
            @Parameter(description = "ID de la orden a buscar", required = true) @PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(orderAssembler.toModel(order));
    }

    @PostMapping
    @Operation(summary = "Crear nueva orden", description = "Crea una nueva orden con los datos enviados")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Orden creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    })
    public ResponseEntity<EntityModel<OrderResponseDTO>> createOrder(
            @Parameter(description = "Datos para crear la orden", required = true)
            @Valid @RequestBody OrderRequestDTO orderRequest) {
        OrderResponseDTO newOrder = orderService.createOrder(orderRequest);
        return new ResponseEntity<>(orderAssembler.toModel(newOrder), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todas las órdenes", description = "Obtiene todas las órdenes existentes")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Órdenes obtenidas"),
        @ApiResponse(responseCode = "204", description = "No hay órdenes para mostrar")
    })
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
    @Operation(summary = "Listar órdenes por fecha", description = "Obtiene todas las órdenes realizadas en una fecha específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Órdenes encontradas"),
        @ApiResponse(responseCode = "204", description = "No hay órdenes en esa fecha")
    })
    public ResponseEntity<CollectionModel<EntityModel<OrderResponseDTO>>> getOrdersByDate(
            @Parameter(description = "Fecha de la orden (YYYY-MM-DD)", required = true)
            @RequestParam("orderDate") LocalDate orderDate) {
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
    @Operation(summary = "Actualizar estado de la orden", description = "Actualiza el estado de una orden específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<EntityModel<OrderResponseDTO>> updateOrderStatus(
            @Parameter(description = "ID de la orden a actualizar", required = true) @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la orden", required = true) @RequestBody OrderStatusUpdateRequestDTO request) {
        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(id, request.getNewStatus());
        return ResponseEntity.ok(orderAssembler.toModel(updatedOrder));
    }

}
