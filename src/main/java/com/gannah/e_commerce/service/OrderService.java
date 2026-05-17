package com.gannah.e_commerce.service;

import com.gannah.e_commerce.DTO.request.OrderRequest;
import com.gannah.e_commerce.DTO.response.OrderItemResponse;
import com.gannah.e_commerce.DTO.response.OrderResponse;
import com.gannah.e_commerce.model.*;
import com.gannah.e_commerce.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public OrderResponse placeOrder(String email, OrderRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(()-> new RuntimeException("Cart is empty"));
        if(cart.getCartItems()==null || cart.getCartItems().isEmpty()){
            throw new RuntimeException("Cart is empty");
        }

        Address address = addressRepository.findById(request.getAddressId()).orElseThrow(()-> new RuntimeException("Address not found"));

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            if(product.getStockQuantity() < cartItem.getQuantity()){
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            totalPrice  = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        Order order =Order.builder()
                .user(user)
                .address(address)
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .build();
        orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            orderItems.add(orderItem);

           product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());

           productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        orderRepository.save(order);

        Payment payment = Payment.builder()
                .order(order)
                .method(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .amount(totalPrice)
                .build();

        paymentRepository.save(payment);

        cartItemRepository.deleteByCartId(cart.getId());

        return mapToResponse(order, payment);
    }

    public List<OrderResponse> getUserOrdes (String email){
        User user =  userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));

        return orderRepository.findByUserId(user.getId()).stream()
                .map(order -> {
                    Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
                    return mapToResponse(order, payment);
                })
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(String email,Long orderId){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId).orElseThrow(()-> new RuntimeException("Order not found"));

        if(!order.getUser().getId().equals(user.getId())){
            throw new RuntimeException("Unauthorized access");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);

        return mapToResponse(order, payment);
    }

    public OrderResponse cancelOrder(String email,Long orderId){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new RuntimeException("Order not found"));
        if(!order.getUser().getId().equals(user.getId())){
            throw new RuntimeException("Unauthorized access");
        }

        if(!order.getStatus().equals(OrderStatus.PENDING)){
            throw new RuntimeException("Only pending orders can be cancelled");
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        return mapToResponse(order, payment);
    }

    public OrderResponse updateOrderStatus(Long orderId,OrderStatus orderStatus){
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new RuntimeException("Order not found"));
        order.setStatus(orderStatus);
        orderRepository.save(order);
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        return mapToResponse(order, payment);
    }

    private OrderResponse mapToResponse(Order order, Payment payment){
        List<OrderItemResponse> items = order.getOrderItems() == null
                ? List.of()
                :order.getOrderItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .items(items)
                .totalPrice(order.getTotalPrice())
                .paymentMethod(payment != null ? payment.getMethod().name() : null)
                .paymentStatus(payment != null ? payment.getStatus().name() : null)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemResponse mapItemToResponse(OrderItem item){
        BigDecimal subtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImage(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(subtotal)
                .build();
    }
}
