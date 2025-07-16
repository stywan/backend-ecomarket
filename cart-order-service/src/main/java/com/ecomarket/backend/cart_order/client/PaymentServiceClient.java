package com.ecomarket.backend.cart_order.client;

import com.ecomarket.backend.cart_order.DTO.request.TransactionRequestDTO;
import com.ecomarket.backend.cart_order.DTO.response.TransactionResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${payment.service.base-url}")
    private String paymentServiceBaseUrl;
    @Value("${payment.service.transactions-path}")
    private String paymentServiceTransactionsPath;

    public PaymentServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TransactionResponseDTO createPaymentTransaction(TransactionRequestDTO request) {
        String url = paymentServiceBaseUrl + paymentServiceTransactionsPath;
        try {
            return restTemplate.postForObject(url, request, TransactionResponseDTO.class);
        } catch (HttpClientErrorException ex) {
            System.err.println("Error calling Payment Service to create transaction: " + ex.getResponseBodyAsString());
            throw new IllegalArgumentException("Failed to create payment transaction: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            System.err.println("Unexpected error communicating with Payment Service: " + ex.getMessage());
            throw new RuntimeException("Failed to create payment transaction.", ex);
        }
    }
}