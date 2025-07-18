package com.ecomarket.backend.catalog_product.service;

import com.ecomarket.backend.catalog_product.DTO.request.ReviewRequest;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.Product;
import com.ecomarket.backend.catalog_product.model.Review;
import com.ecomarket.backend.catalog_product.repository.ProductRepository;
import com.ecomarket.backend.catalog_product.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private ProductRepository productRepo;

    @InjectMocks
    private ReviewService reviewService;

    // Datos de prueba comunes
    private Product testProduct;
    private ReviewRequest reviewRequest;
    private Review testReview;
    private Review anotherTestReview;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .build();

        reviewRequest = ReviewRequest.builder()
                .productId(1L)
                .userId(100L)
                .rating(5)
                .comment("Excellent product, highly recommended!")
                .build();

        testReview = Review.builder()
                .id(1L)
                .product(testProduct)
                .userId(100L)
                .rating(5)
                .comment("Excellent product, highly recommended!")
                .reviewDate(LocalDateTime.now())
                .build();

        anotherTestReview = Review.builder()
                .id(2L)
                .product(testProduct)
                .userId(101L)
                .rating(4)
                .comment("Good product, fast delivery.")
                .reviewDate(LocalDateTime.now().minusDays(1))
                .build();
    }

    // --- Tests para createReview ---
    @Test
    @DisplayName("createReview - Should create a review successfully")
    void createReview_success() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(reviewRepo.save(any(Review.class))).thenAnswer(invocation -> {
            Review reviewToSave = invocation.getArgument(0);
            reviewToSave.setId(1L); // Simular que JPA asigna un ID
            return reviewToSave;
        });

        Review createdReview = reviewService.createReview(reviewRequest);

        assertNotNull(createdReview);
        assertEquals(testReview.getRating(), createdReview.getRating());
        assertEquals(testReview.getComment(), createdReview.getComment());
        assertEquals(testProduct.getId(), createdReview.getProduct().getId());
        assertNotNull(createdReview.getReviewDate()); // Verify reviewDate is set

        verify(productRepo, times(1)).findById(reviewRequest.getProductId());
        verify(reviewRepo, times(1)).save(any(Review.class));

        // Optional: Use ArgumentCaptor to verify the captured review's reviewDate is recent
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepo).save(reviewCaptor.capture());
        assertTrue(reviewCaptor.getValue().getReviewDate().isAfter(LocalDateTime.now().minusSeconds(5))); // Check it's very recent
    }

    @Test
    @DisplayName("createReview - Should throw ResourceNotFoundException if product not found")
    void createReview_productNotFound() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.createReview(reviewRequest);
        });

        assertEquals("Product not found", thrown.getMessage());
        verify(productRepo, times(1)).findById(reviewRequest.getProductId());
        verify(reviewRepo, never()).save(any(Review.class)); // Review should not be saved
    }

    // --- Tests para getReviews ---
    @Test
    @DisplayName("getReviews - Should return a list of reviews for a given product ID")
    void getReviews_success() {
        when(reviewRepo.findByProduct_Id(anyLong())).thenReturn(Arrays.asList(testReview, anotherTestReview));

        List<Review> reviews = reviewService.getReviews(testProduct.getId());

        assertNotNull(reviews);
        assertFalse(reviews.isEmpty());
        assertEquals(2, reviews.size());
        assertEquals(testReview.getId(), reviews.get(0).getId());
        assertEquals(anotherTestReview.getId(), reviews.get(1).getId());
        verify(reviewRepo, times(1)).findByProduct_Id(testProduct.getId());
    }

    @Test
    @DisplayName("getReviews - Should return an empty list if no reviews found for product ID")
    void getReviews_emptyList() {
        when(reviewRepo.findByProduct_Id(anyLong())).thenReturn(Collections.emptyList());

        List<Review> reviews = reviewService.getReviews(99L); // Product ID with no reviews

        assertNotNull(reviews);
        assertTrue(reviews.isEmpty());
        verify(reviewRepo, times(1)).findByProduct_Id(99L);
    }
}
