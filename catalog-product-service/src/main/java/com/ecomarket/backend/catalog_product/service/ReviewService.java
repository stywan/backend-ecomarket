package com.ecomarket.backend.catalog_product.service;

import com.ecomarket.backend.catalog_product.DTO.request.ReviewRequest;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.Product;
import com.ecomarket.backend.catalog_product.model.Review;
import com.ecomarket.backend.catalog_product.repository.ProductRepository;
import com.ecomarket.backend.catalog_product.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final ProductRepository productRepo;

    public Review createReview(ReviewRequest request) {
        Product product = productRepo.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = Review.builder()
                .product(product)
                .userId(request.getUserId())
                .rating(request.getRating())
                .comment(request.getComment())
                .reviewDate(LocalDateTime.now())
                .build();

        return reviewRepo.save(review);
    }

    public List<Review> getReviews(Long productId) {
        return reviewRepo.findByProduct_Id(productId);
    }
}
