package com.ecomarket.backend.cart_order.assembler;

import com.ecomarket.backend.cart_order.DTO.response.OrderResponseDTO;
import com.ecomarket.backend.cart_order.controller.OrderController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrderAssembler implements RepresentationModelAssembler<OrderResponseDTO, EntityModel<OrderResponseDTO>> {

    @Override
    public EntityModel<OrderResponseDTO> toModel(OrderResponseDTO order) {
        // Enlaces básicos para una orden individual
        EntityModel<OrderResponseDTO> orderModel = EntityModel.of(order,
                linkTo(methodOn(OrderController.class).getOrderById(order.getId())).withSelfRel(),
                linkTo(methodOn(OrderController.class).getAllOrders()).withRel("orders")
        );

        if (!order.getOrderStatus().equals("DELIVERED") && !order.getOrderStatus().equals("CANCELLED")) {
            orderModel.add(linkTo(methodOn(OrderController.class).updateOrderStatus(order.getId(), null)).withRel("updateStatus"));
        }

        return orderModel;
    }
}
