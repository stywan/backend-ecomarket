package com.ecomarket.backend.cart_order.client;

import com.ecomarket.backend.cart_order.DTO.ProductResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductServiceClient {

    private final RestTemplate restTemplate;
    // Asegúrate de que esta URL base sea correcta para tu servicio de productos
    private final String productServiceBaseUrl = "http://localhost:8082/api/v1/products";

    public ProductServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductResponseDTO getProductById(Long productId) {
        String url = productServiceBaseUrl + "/" + productId;
        try {
            return restTemplate.getForObject(url, ProductResponseDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            System.err.println("Product not found: " + productId);
            return null;
        } catch (Exception ex) {
            System.err.println("Error calling Product Service for product ID " + productId + ": " + ex.getMessage());
            throw new RuntimeException("Failed to retrieve product details.", ex);
        }
    }
}


