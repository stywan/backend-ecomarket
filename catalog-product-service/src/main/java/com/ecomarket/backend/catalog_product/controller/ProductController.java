package com.ecomarket.backend.catalog_product.controller;

import com.ecomarket.backend.catalog_product.DTO.ProductImageRequest;
import com.ecomarket.backend.catalog_product.DTO.ProductRequest;
import com.ecomarket.backend.catalog_product.DTO.ProductResponse;
import com.ecomarket.backend.catalog_product.assembler.ProductAssembler;
import com.ecomarket.backend.catalog_product.model.Product;
import com.ecomarket.backend.catalog_product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductAssembler productAssembler;

    @PostMapping
    public EntityModel<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productService.createProduct(request);
        return productAssembler.toModel(product);
    }

    @GetMapping("/{id}")
    public EntityModel<ProductResponse> getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        return productAssembler.toModel(product);
    }

    @PutMapping("/{id}")
    public EntityModel<ProductResponse> updateProduct(@PathVariable Long id,
                                                      @Valid @RequestBody ProductRequest request) {
        Product product = productService.updateProduct(id, request);
        return productAssembler.toModel(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    public EntityModel<ProductResponse> addImage(@PathVariable Long id,
                                                 @Valid @RequestBody ProductImageRequest request) {
        Product product = productService.addImage(id, request);
        return productAssembler.toModel(product);
    }




    @GetMapping
    public CollectionModel<EntityModel<ProductResponse>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId
    ) {
        List<EntityModel<ProductResponse>> products = productService.searchProducts(name, sku, categoryId, brandId)
                .stream()
                .map(productAssembler::toModel)
                .toList();
        return CollectionModel.of(products);
    }
}