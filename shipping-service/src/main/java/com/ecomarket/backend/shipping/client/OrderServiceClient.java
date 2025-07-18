package com.ecomarket.backend.shipping.client;

import com.ecomarket.backend.shipping.DTO.response.OrderResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Component
public class OrderServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.order.base-url}")
    private String orderServiceBaseUrl;

    @Value("${service.order.get-order-path}")
    private String orderPath;

    public OrderServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OrderResponseDTO getOrderById(Long orderId) {
        String url = orderServiceBaseUrl + orderPath + "/" + orderId;

        try {
            return restTemplate.getForObject(url, OrderResponseDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            System.err.println("Order not found with ID: " + orderId);
            return null;
        } catch (Exception ex) {
            System.err.println("Error calling Order Service for order ID " + orderId + ": " + ex.getMessage());
            throw new RuntimeException("Failed to retrieve order details.", ex);
        }
    }
}
