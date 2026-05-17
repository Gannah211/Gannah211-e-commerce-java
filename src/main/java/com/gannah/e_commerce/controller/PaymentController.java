package com.gannah.e_commerce.controller;

import com.gannah.e_commerce.DTO.response.PaymentResponse;
import com.gannah.e_commerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(getCurrentUserEmail(), orderId)
        );
    }

    @PutMapping("/order/{orderId}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
                paymentService.confirmPayment(getCurrentUserEmail(), orderId)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @PutMapping("/order/{orderId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.refundPayment(orderId));
    }

}
