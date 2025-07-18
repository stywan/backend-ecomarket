package com.ecomarket.backend.catalog_product.assembler;

import com.ecomarket.backend.catalog_product.DTO.response.ReviewResponse;
import com.ecomarket.backend.catalog_product.controller.ProductController;
import com.ecomarket.backend.catalog_product.controller.ReviewController;
import com.ecomarket.backend.catalog_product.model.Review;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ReviewAssembler implements RepresentationModelAssembler<Review, EntityModel<ReviewResponse>> {

    @Override
    public EntityModel<ReviewResponse> toModel(Review review) {
        ReviewResponse response = ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewDate(review.getReviewDate())
                .build();

        return EntityModel.of(response,
                linkTo(methodOn(ReviewController.class).getReviews(review.getProduct().getId())).withRel("productReviews"),
                linkTo(methodOn(ProductController.class).getProduct(review.getProduct().getId())).withRel("product")
        );
    }
}