package com.ecomarket.backend.catalog_product.controller;

import com.ecomarket.backend.catalog_product.DTO.request.ReviewRequest;
import com.ecomarket.backend.catalog_product.DTO.response.ReviewResponse;
import com.ecomarket.backend.catalog_product.assembler.ReviewAssembler;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.Review;
import com.ecomarket.backend.catalog_product.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Gestión de reseñas de productos")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewAssembler reviewAssembler;

    @PostMapping
    @Operation(summary = "Crear una reseña", description = "Crea una nueva reseña para un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reseña creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<ReviewResponse>> createReview(
            @Parameter(description = "Datos de la reseña", required = true)
            @Valid @RequestBody ReviewRequest request) {
        try {
            Review review = reviewService.createReview(request);
            return ResponseEntity.status(201).body(reviewAssembler.toModel(review));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Obtener reseñas por producto", description = "Obtiene todas las reseñas de un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reseñas"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado o sin reseñas")
    })
    public ResponseEntity<CollectionModel<EntityModel<ReviewResponse>>> getReviews(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long productId) {
        try {
            List<EntityModel<ReviewResponse>> reviews = reviewService.getReviews(productId).stream()
                    .map(reviewAssembler::toModel)
                    .toList();
            if (reviews.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(CollectionModel.of(reviews));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}