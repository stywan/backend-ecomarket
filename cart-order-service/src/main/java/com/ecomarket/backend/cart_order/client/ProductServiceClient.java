package com.ecomarket.backend.cart_order.client;

import com.ecomarket.backend.cart_order.DTO.request.InventoryOperationRequestDTO;
import com.ecomarket.backend.cart_order.DTO.response.InventoryResponseDTO;
import com.ecomarket.backend.cart_order.DTO.response.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${product.service.base-url}")
    private String productServiceBaseUrl;

    @Value("${product.service.products-path}")
    private String productsPath;

    @Value("${product.service.inventory-path}")
    private String inventoryPath;


    public ProductServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductResponseDTO getProductById(Long productId) {
        String url = productServiceBaseUrl + productsPath + "/" + productId;
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

    public InventoryResponseDTO getProductInventory(Long productId) {
        String url = productServiceBaseUrl + inventoryPath + "/" + productId;
        try {
            return restTemplate.getForObject(url, InventoryResponseDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            System.err.println("Inventory not found for product ID: " + productId);
            return null;
        } catch (Exception ex) {
            System.err.println("Error getting inventory for product ID " + productId + ": " + ex.getMessage());
            throw new RuntimeException("Failed to retrieve inventory details.", ex);
        }
    }

    public InventoryResponseDTO performInventoryOperation(Long productId, String operationType, int quantity) {
        String url = productServiceBaseUrl + inventoryPath + "/" + productId + "/operation";
        InventoryOperationRequestDTO request = new InventoryOperationRequestDTO(operationType, quantity);
        try {
            return restTemplate.postForObject(url, request, InventoryResponseDTO.class);
        } catch (HttpClientErrorException ex) {
            System.err.println("Inventory operation failed for product " + productId + ", type " + operationType + ", quantity " + quantity + ": " + ex.getResponseBodyAsString());
            throw new IllegalArgumentException("Inventory operation failed: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            System.err.println("Error performing inventory operation for product ID " + productId + ": " + ex.getMessage());
            throw new RuntimeException("Failed to perform inventory operation.", ex);
        }
    }
}


