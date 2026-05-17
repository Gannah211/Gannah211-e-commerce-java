package com.gannah.e_commerce.service;

import com.gannah.e_commerce.DTO.request.CartItemRequest;
import com.gannah.e_commerce.DTO.response.CartItemResponse;
import com.gannah.e_commerce.DTO.response.CartResponse;
import com.gannah.e_commerce.model.Cart;
import com.gannah.e_commerce.model.CartItem;
import com.gannah.e_commerce.model.Product;
import com.gannah.e_commerce.model.User;
import com.gannah.e_commerce.repository.CartItemRepository;
import com.gannah.e_commerce.repository.CartRepository;
import com.gannah.e_commerce.repository.ProductRepository;
import com.gannah.e_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return mapToResponse(cart);
    }

    public CartResponse addItem(String email , CartItemRequest request){
        Cart cart = getOrCreateCart(email);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(()-> new  RuntimeException("Product not found"));

        if(!product.isActive()){
            throw new RuntimeException("Product is not active");
        }
        if(product.getStockQuantity()<request.getQuantity()){
            throw new RuntimeException("Not enough stock available");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId() ,product.getId());
        if(existingItem.isPresent()){
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity()+request.getQuantity());
            cartItemRepository.save(item);
        }else{
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }

        List<CartItem> updatedItems = cartItemRepository.findAllByCartId(cart.getId());
        cart.setCartItems(updatedItems);
        return mapToResponse(cart);
    }

    public CartResponse updateItemQuantity(String email, Long itemId, Integer quantity){
        Cart cart = getOrCreateCart(email);

        CartItem item = cartItemRepository.findById(itemId).orElseThrow(()-> new  RuntimeException("Item not found"));

        if(!item.getCart().getId().equals(cart.getId())){
            throw new RuntimeException("Unauthorized");
        }
        if(quantity<=0){
            cartItemRepository.delete(item);
        }else{
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        cart= cartRepository.findById(cart.getId()).get();
        return mapToResponse(cart);
    }

    public CartResponse removeItem(String email, Long itemId){
        Cart cart = getOrCreateCart(email);

        CartItem item = cartItemRepository.findById(itemId).orElseThrow(()-> new RuntimeException("Item not found"));

        if(!item.getCart().getId().equals(cart.getId())){
            throw new RuntimeException("Unauthorized");
        }
        cartItemRepository.delete(item);
        cart=cartRepository.findById(cart.getId()).get();
        return mapToResponse(cart);
    }

    public void clearCart(String email){
        Cart cart = getOrCreateCart(email);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private Cart getOrCreateCart(String email){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));

        return cartRepository.findByUserId(user.getId()).orElseGet(()->{
            Cart newCart = Cart.builder()
                    .user(user)
                    .build();
            return cartRepository.save(newCart);
        });
    }

    private CartResponse mapToResponse(Cart cart){
        List<CartItemResponse> items = cart.getCartItems() == null ? List.of() : cart.getCartItems().stream().map(this::mapItemToResponse).collect(Collectors.toList());

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalPrice(totalPrice)
                .totalItems(totalItems)
                .build();
    }
    private CartItemResponse mapItemToResponse(CartItem item){
        BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImage(item.getProduct().getImageUrl())
                .unitPrice(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
