package com.gannah.e_commerce.service;

import com.gannah.e_commerce.DTO.request.ReviewRequest;
import com.gannah.e_commerce.DTO.response.ReviewResponse;
import com.gannah.e_commerce.model.OrderStatus;
import com.gannah.e_commerce.model.Product;
import com.gannah.e_commerce.model.Review;
import com.gannah.e_commerce.model.User;
import com.gannah.e_commerce.repository.OrderRepository;
import com.gannah.e_commerce.repository.ProductRepository;
import com.gannah.e_commerce.repository.ReviewRepository;
import com.gannah.e_commerce.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ReviewResponse addReview(String email,ReviewRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found "));

        Product product = productRepository.findById(request.getProductId()).orElseThrow(()-> new RuntimeException("Product not found"));

        if(reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new RuntimeException("You have already reviewed this product");
        }
        boolean hasPurchashed = orderRepository.findByUserId(user.getId())
                .stream()
                .filter(order -> order.getStatus().equals(OrderStatus.DELIVERED))
                .flatMap(order-> order.getOrderItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(product.getId()));

        if(!hasPurchashed) {
            throw new RuntimeException("You can only review products you have purchased");
        }
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        return mapToResponse(reviewRepository.save(review));
    }

    public List<ReviewResponse> getProductReviews(Long productId){
        productRepository.findById(productId).orElseThrow(()-> new RuntimeException("Product not found"));

        return reviewRepository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getUserReviews(String email){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found "));

        return reviewRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReviewResponse updateReview(String email,Long reviewId, ReviewRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found "));
        Review review = reviewRepository.findById(reviewId).orElseThrow(()-> new RuntimeException("Review not found"));

        if(!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return mapToResponse(reviewRepository.save(review));
    }

    public void deleteReview(String email,Long reviewId){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found "));
        Review review = reviewRepository.findById(reviewId).orElseThrow(()-> new RuntimeException("Review not found"));

        if(!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        reviewRepository.delete(review);
    }

    public Double getAverageRating(Long productId){
        List<Review> reviews = reviewRepository.findByProductId(productId);

        if(reviews.isEmpty()) return 0.0;

        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    public ReviewResponse mapToResponse(Review review){
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userName(review.getUser().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
