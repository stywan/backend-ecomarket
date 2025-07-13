package com.ecomarket.backend.cart_order.service;

import com.ecomarket.backend.cart_order.exception.InvalidReturnOperationException;
import com.ecomarket.backend.cart_order.exception.ResourceNotFoundException;
import com.ecomarket.backend.cart_order.model.Order;
import com.ecomarket.backend.cart_order.model.Return;
import com.ecomarket.backend.cart_order.repository.OrderRepository;
import com.ecomarket.backend.cart_order.repository.ReturnRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.ecomarket.backend.cart_order.model.OrderItem;
import com.ecomarket.backend.cart_order.repository.OrderItemRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public Return requestReturn(Long orderId, Long orderItemId, String reason) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderItem orderItem = null;
        if (orderItemId != null) {
            orderItem = orderItemRepository.findById(orderItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        }

        Return r = Return.builder()
                .order(order)
                .orderItem(orderItem)
                .reason(reason)
                .requestDate(LocalDateTime.now())
                .returnStatus(Return.ReturnStatus.REQUESTED)
                .build();

        return returnRepository.save(r);
    }


    public Return approveReturn(Long returnId, Double refundAmount) {
        Return r = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));

        if (r.getReturnStatus() != Return.ReturnStatus.REQUESTED) {
            throw new InvalidReturnOperationException("Only REQUESTED returns can be approved.");
        }

        r.setReturnStatus(Return.ReturnStatus.APPROVED);
        r.setRefundAmount(refundAmount);
        r.setRefundDate(LocalDateTime.now());

        return returnRepository.save(r);
    }


    public Return rejectReturn(Long returnId, String reason) {
        Return r = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));

        r.setReturnStatus(Return.ReturnStatus.REJECTED);
        r.setReason(reason);

        return returnRepository.save(r);
    }


    public Return completeReturn(Long returnId) {
        Return r = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));

        r.setReturnStatus(Return.ReturnStatus.COMPLETED);
        r.setRefundDate(LocalDateTime.now());

        return returnRepository.save(r);
    }
}
