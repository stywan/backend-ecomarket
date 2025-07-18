package com.ecomarket.backend.catalog_product.assembler;

import com.ecomarket.backend.catalog_product.DTO.response.BrandResponse;
import com.ecomarket.backend.catalog_product.DTO.response.CategoryResponse;
import com.ecomarket.backend.catalog_product.DTO.response.ProductImageResponse;
import com.ecomarket.backend.catalog_product.DTO.response.ProductResponse;
import com.ecomarket.backend.catalog_product.controller.InventoryController;
import com.ecomarket.backend.catalog_product.controller.ProductController;
import com.ecomarket.backend.catalog_product.controller.ReviewController;
import com.ecomarket.backend.catalog_product.model.Product;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductAssembler implements RepresentationModelAssembler<Product, EntityModel<ProductResponse>> {

    @Override
    public EntityModel<ProductResponse> toModel(Product product) {
        ProductResponse response = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .sku(product.getSku())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .status(product.getStatus().name())
                .category(CategoryResponse.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .description(product.getCategory().getDescription()).build())
                .brand(BrandResponse.builder()
                        .id(product.getBrand().getId())
                        .name(product.getBrand().getName())
                        .description(product.getBrand().getDescription()).build())
                .images(product.getImages().stream()
                        .map(img -> ProductImageResponse.builder()
                                .id(img.getId())
                                .url(img.getUrl()).build())
                        .toList())
                .build();

        return EntityModel.of(response,linkTo(methodOn(ProductController.class).getProduct(product.getId())).withSelfRel(),
                linkTo(methodOn(ProductController.class).updateProduct(product.getId(), null)).withRel("update"),
                linkTo(methodOn(ProductController.class).deleteProduct(product.getId())).withRel("delete"),
                linkTo(methodOn(ProductController.class).addImage(product.getId(), null)).withRel("addImage"),
                linkTo(methodOn(InventoryController.class).getInventory(product.getId())).withRel("inventory"),
                linkTo(methodOn(ReviewController.class).getReviews(product.getId())).withRel("reviews")
        );
    }
}
