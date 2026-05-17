package com.gannah.e_commerce.controller;

import com.gannah.e_commerce.DTO.request.CartItemRequest;
import com.gannah.e_commerce.DTO.response.CartResponse;
import com.gannah.e_commerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(){
        return  ResponseEntity.ok(cartService.getCart(getCurrentUserEmail()));
    }
    @PostMapping("/items")
    public ResponseEntity<?> addItem(@Valid @RequestBody CartItemRequest request){
        System.out.println("REQUEST RECEIVED: " + request);

        if (request == null) {
            return ResponseEntity.badRequest().body("Request body is null");
        }
        if (request.getProductId() == null) {
            return ResponseEntity.badRequest().body("ProductId is null");
        }
        if (request.getQuantity() == null) {
            return ResponseEntity.badRequest().body("Quantity is null");
        }
        return ResponseEntity.ok(cartService.addItem(getCurrentUserEmail(), request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity (@PathVariable Long itemI , @RequestParam Integer quantity){
        return ResponseEntity.ok(cartService.updateItemQuantity(getCurrentUserEmail(), itemI, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId){
        return ResponseEntity.ok(cartService.removeItem(getCurrentUserEmail(), itemId));
    }

    @DeleteMapping
    public ResponseEntity<String> clearCart(){
        cartService.clearCart(getCurrentUserEmail());
        return ResponseEntity.ok("Cart has been cleared");
    }
}
