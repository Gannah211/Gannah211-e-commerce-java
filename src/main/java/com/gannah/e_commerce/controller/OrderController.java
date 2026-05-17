package com.gannah.e_commerce.controller;

import com.gannah.e_commerce.DTO.request.OrderRequest;
import com.gannah.e_commerce.DTO.response.OrderResponse;
import com.gannah.e_commerce.model.OrderStatus;
import com.gannah.e_commerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder( @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(getCurrentUserEmail(), request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders() {
        return ResponseEntity.ok(orderService.getUserOrdes(getCurrentUserEmail()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(getCurrentUserEmail(), orderId));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(getCurrentUserEmail(), orderId));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize(("hasRole('ADMIN')"))
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId, @RequestBody OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

}
