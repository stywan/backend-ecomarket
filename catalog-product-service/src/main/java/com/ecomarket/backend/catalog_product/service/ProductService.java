package com.ecomarket.backend.catalog_product.service;

import com.ecomarket.backend.catalog_product.DTO.request.ProductImageRequest;
import com.ecomarket.backend.catalog_product.DTO.request.ProductRequest;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.*;
import com.ecomarket.backend.catalog_product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;
    private final ProductImageRepository imageRepo;
    private final InventoryRepository inventoryRepo;

    public Product createProduct(ProductRequest request) {

        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Brand brand = brandRepo.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .sku(request.getSku())
                .category(category)
                .brand(brand)
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .status(Product.ProductStatus.ACTIVE)
                .creationDate(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .build();

        Product savedProduct = productRepo.save(product);

        Inventory inventory = Inventory.builder()
                .product(savedProduct)
                .availableQuantity(0)
                .location("Default Location")
                .lastUpdate(LocalDateTime.now())
                .build();

        inventoryRepo.save(inventory);

        return savedProduct;
    }

    public Product updateProduct(Long id, ProductRequest request) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Brand brand = brandRepo.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setCategory(category);
        product.setBrand(brand);
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());
        product.setLastUpdate(LocalDateTime.now());

        return productRepo.save(product);

    }

    public void deleteProduct(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setStatus(Product.ProductStatus.INACTIVE);
        productRepo.save(product);
    }

    public Product addImage(Long productId, ProductImageRequest request) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductImage image = ProductImage.builder()
                .product(product)
                .url(request.getUrl())
                .build();

        product.getImages().add(image);
        imageRepo.save(image);
        return productRepo.save(product);
    }

    public void removeImage(Long imageId) {
        ProductImage image = imageRepo.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        imageRepo.delete(image);
    }

    public Product getProduct(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public List<Product> searchProducts(String name, String sku, Long categoryId, Long brandId) {
        if (name != null) {
            return productRepo.findByNameContaining(name);
        }
        if (sku != null) {
            return productRepo.findBySku(sku).map(List::of).orElse(List.of());
        }
        if (categoryId != null) {
            return productRepo.findByCategory_Id(categoryId);
        }
        if (brandId != null) {
            return productRepo.findByBrand_Id(brandId);
        }
        return productRepo.findAll();
    }
}
