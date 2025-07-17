package com.ecomarket.backend.catalog_product.controller;

import com.ecomarket.backend.catalog_product.DTO.ReviewRequest;
import com.ecomarket.backend.catalog_product.DTO.ReviewResponse;
import com.ecomarket.backend.catalog_product.assembler.ReviewAssembler;
import com.ecomarket.backend.catalog_product.model.Review;
import com.ecomarket.backend.catalog_product.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewAssembler reviewAssembler;

    @PostMapping
    public EntityModel<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        Review review = reviewService.createReview(request);
        return reviewAssembler.toModel(review);
    }

    @GetMapping("/product/{productId}")
    public CollectionModel<EntityModel<ReviewResponse>> getReviews(@PathVariable Long productId) {
        List<EntityModel<ReviewResponse>> reviews = reviewService.getReviews(productId).stream()
                .map(reviewAssembler::toModel)
                .toList();
        return CollectionModel.of(reviews);
    }
}