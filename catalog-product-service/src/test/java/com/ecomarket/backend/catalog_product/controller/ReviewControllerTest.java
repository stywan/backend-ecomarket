package com.ecomarket.backend.catalog_product.controller;

import com.ecomarket.backend.catalog_product.DTO.request.ReviewRequest;
import com.ecomarket.backend.catalog_product.DTO.response.ReviewResponse;
import com.ecomarket.backend.catalog_product.assembler.ReviewAssembler;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.Product;
import com.ecomarket.backend.catalog_product.model.Review;
import com.ecomarket.backend.catalog_product.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@DisplayName("ReviewController Unit Tests")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private ReviewAssembler reviewAssembler;

    // Datos de prueba comunes
    private ReviewRequest reviewRequest;
    private Review testReview;
    private ReviewResponse testReviewResponse;
    private EntityModel<ReviewResponse> testReviewEntityModel; // This will now be for a specific product
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder().id(1L).name("Test Product").build();

        reviewRequest = ReviewRequest.builder()
                .productId(1L)
                .userId(100L)
                .rating(5)
                .comment("Great product!")
                .build();

        testReview = Review.builder()
                .id(1L)
                .product(testProduct)
                .userId(100L)
                .rating(5)
                .comment("Great product!")
                .reviewDate(LocalDateTime.now())
                .build();

        testReviewResponse = ReviewResponse.builder()
                .id(1L)
                .productId(1L)
                .userId(100L)
                .rating(5)
                .comment("Great product!")
                .reviewDate(testReview.getReviewDate())
                .build();

        testReviewEntityModel = EntityModel.of(testReviewResponse,
                linkTo(methodOn(ReviewController.class).getReviews(testReviewResponse.getProductId())).withSelfRel()); // Link to the product's reviews

        when(reviewAssembler.toModel(any(Review.class))).thenReturn(testReviewEntityModel);
    }

    // --- Tests para POST /api/v1/reviews (createReview) ---
    @Test
    @DisplayName("POST / - Should create a review and return 201 Created")
    void createReview_success() throws Exception {
        when(reviewService.createReview(any(ReviewRequest.class))).thenReturn(testReview);

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testReview.getId()))
                .andExpect(jsonPath("$.rating").value(testReview.getRating()))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(reviewService, times(1)).createReview(any(ReviewRequest.class));
        verify(reviewAssembler, times(1)).toModel(testReview);
    }

    @Test
    @DisplayName("POST / - Should return 400 Bad Request for invalid review request")
    void createReview_invalidRequest() throws Exception {
        ReviewRequest invalidRequest = ReviewRequest.builder()
                .productId(1L)
                .userId(100L)
                .rating(0) // Invalid: Rating typically 1-5
                .comment("") // Invalid: Comment @NotBlank
                .build();

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists()); // Check for validation error message

        verify(reviewService, never()).createReview(any(ReviewRequest.class));
        verify(reviewAssembler, never()).toModel(any(Review.class));
    }

    @Test
    @DisplayName("POST / - Should return 404 Not Found if product not found (service throws ResourceNotFoundException)")
    void createReview_productNotFound() throws Exception {
        // The controller catches ResourceNotFoundException and returns 404
        when(reviewService.createReview(any(ReviewRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotHaveJsonPath()); // Expect empty body as per controller's catch block

        verify(reviewService, times(1)).createReview(any(ReviewRequest.class));
        verify(reviewAssembler, never()).toModel(any(Review.class));
    }

    // --- Tests para GET /api/v1/reviews/product/{productId} (getReviews) ---
    @Test
    @DisplayName("GET /product/{productId} - Should return reviews when found")
    void getReviews_success() throws Exception {
        Review anotherReview = Review.builder()
                .id(2L)
                .product(testProduct)
                .userId(101L)
                .rating(4)
                .comment("Good product!")
                .reviewDate(LocalDateTime.now().minusDays(1))
                .build();
        List<Review> reviews = Arrays.asList(testReview, anotherReview);

        // Prepare a distinct EntityModel for anotherReview with its own link
        EntityModel<ReviewResponse> anotherReviewEntityModel = EntityModel.of(
                ReviewResponse.builder().id(2L).productId(1L).userId(101L).rating(4).comment("Good product!").reviewDate(anotherReview.getReviewDate()).build(),
                linkTo(methodOn(ReviewController.class).getReviews(testProduct.getId())).withSelfRel());

        when(reviewService.getReviews(anyLong())).thenReturn(reviews);
        // Mock assembler calls for each product in the list explicitly if they return different models
        when(reviewAssembler.toModel(testReview)).thenReturn(testReviewEntityModel);
        when(reviewAssembler.toModel(anotherReview)).thenReturn(anotherReviewEntityModel);


        mockMvc.perform(get("/api/v1/reviews/product/{productId}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reviewResponseList.length()").value(2))
                .andExpect(jsonPath("$._embedded.reviewResponseList[0].id").value(testReview.getId()))
                .andExpect(jsonPath("$._embedded.reviewResponseList[1].id").value(anotherReview.getId()));

        verify(reviewService, times(1)).getReviews(testProduct.getId());
        verify(reviewAssembler, times(1)).toModel(testReview);
        verify(reviewAssembler, times(1)).toModel(anotherReview);
    }

    @Test
    @DisplayName("GET /product/{productId} - Should return 204 No Content when no reviews found for product")
    void getReviews_noReviews() throws Exception {
        when(reviewService.getReviews(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reviews/product/{productId}", 99L))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotHaveJsonPath()); // Expect empty body

        verify(reviewService, times(1)).getReviews(99L);
        verify(reviewAssembler, never()).toModel(any(Review.class));
    }

    @Test
    @DisplayName("GET /product/{productId} - Should return 404 Not Found if service throws ResourceNotFoundException")
    void getReviews_productNotFoundByService() throws Exception {
        // Simular que el servicio lanza ResourceNotFoundException (aunque tu ReviewService actual no lo hace en getReviews)
        // El controlador la captura y devuelve 404 con cuerpo vacío.
        doThrow(new ResourceNotFoundException("Product not found")).when(reviewService).getReviews(anyLong());

        mockMvc.perform(get("/api/v1/reviews/product/{productId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotHaveJsonPath()); // Expect empty body as per controller's catch block

        verify(reviewService, times(1)).getReviews(99L);
        verify(reviewAssembler, never()).toModel(any(Review.class));
    }
}