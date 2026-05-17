package com.gannah.e_commerce.DTO.request;

import com.gannah.e_commerce.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotNull(message="Address is required")
    private Long addressId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
