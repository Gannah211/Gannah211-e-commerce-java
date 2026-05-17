package com.gannah.e_commerce.service;

import com.gannah.e_commerce.DTO.response.PaymentResponse;
import com.gannah.e_commerce.model.*;
import com.gannah.e_commerce.repository.OrderRepository;
import com.gannah.e_commerce.repository.PaymentRepository;
import com.gannah.e_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentResponse getPaymentByOrderId(String email, Long orderId) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("user not found"));

        Order order = orderRepository.findById(orderId).orElseThrow(()-> new RuntimeException("order not found"));

        if(!order.getUser().getId().equals(user.getId())){
            throw new RuntimeException("unauthorized");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(()-> new RuntimeException("payment not found"));

        return mapToResponse(payment);
    }

    public PaymentResponse confirmPayment(String email, Long orderId) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("user not found"));

        Order order = orderRepository.findById(orderId).orElseThrow(()-> new RuntimeException("order not found"));

        if(!order.getUser().getId().equals(user.getId())){
            throw new RuntimeException("unauthorized");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(()-> new RuntimeException("payment not found"));
        if(!payment.getStatus().equals(PaymentStatus.PENDING)){
            throw new RuntimeException("Payment is already " + payment.getStatus().name());
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return mapToResponse(payment);
    }
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse refundPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(()-> new RuntimeException("payment not found"));

        if(!payment.getStatus().equals(PaymentStatus.PAID)){
            throw new RuntimeException("Only paid payments can be refunded");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        return mapToResponse(payment);
    }
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .method(payment.getMethod().name())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .build();
    }

}

