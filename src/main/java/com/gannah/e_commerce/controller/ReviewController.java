package com.gannah.e_commerce.controller;

import com.gannah.e_commerce.DTO.request.ReviewRequest;
import com.gannah.e_commerce.DTO.response.ReviewResponse;
import com.gannah.e_commerce.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    private String getCurrentUserEmail(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(@Valid @RequestBody ReviewRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addReview(getCurrentUserEmail(), request));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(@PathVariable Long productId){
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long productId){
        return ResponseEntity.ok(reviewService.getAverageRating(productId));
    }

    @GetMapping("/myReviews")
    public ResponseEntity<List<ReviewResponse>> getMyReviews() {
        return ResponseEntity.ok(reviewService.getUserReviews(getCurrentUserEmail()));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long reviewId, @Valid @RequestBody ReviewRequest request){
        return ResponseEntity.ok(reviewService.updateReview(getCurrentUserEmail(), reviewId, request));
    }

    @DeleteMapping("{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId){
        reviewService.deleteReview(getCurrentUserEmail(), reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }
}
