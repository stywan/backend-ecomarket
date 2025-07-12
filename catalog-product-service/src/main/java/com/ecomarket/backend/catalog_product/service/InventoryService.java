package com.ecomarket.backend.catalog_product.service;

import com.ecomarket.backend.catalog_product.DTO.InventoryOperationRequest;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.Inventory;
import com.ecomarket.backend.catalog_product.repository.InventoryRepository;
import com.ecomarket.backend.catalog_product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepo;
    private final ProductRepository productRepo;

    public Inventory handleOperation(Long productId, InventoryOperationRequest request) {
        Inventory inventory = inventoryRepo.findByProduct_Id(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        switch (request.getOperationType().toUpperCase()) {
            case "RESERVE" -> {
                if (inventory.getAvailableQuantity() < request.getQuantity()) {
                    throw new IllegalArgumentException("Not enough stock");
                }
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.getQuantity());
            }
            case "RELEASE", "INCREMENT" -> {
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.getQuantity());
            }
            case "DECREMENT" -> {
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.getQuantity());
            }
            default -> throw new IllegalArgumentException("Invalid operation");
        }

        inventory.setLastUpdate(LocalDateTime.now());
        return inventoryRepo.save(inventory);
    }

    public Inventory getInventory(Long productId) {
        return inventoryRepo.findByProduct_Id(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    }
}