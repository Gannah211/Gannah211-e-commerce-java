package com.gannah.e_commerce.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String status;
    private List<OrderItemResponse> items;
    private BigDecimal totalPrice;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime createdAt;
}
