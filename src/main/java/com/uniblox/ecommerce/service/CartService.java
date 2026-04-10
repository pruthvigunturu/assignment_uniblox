package com.uniblox.ecommerce.service;

import com.uniblox.ecommerce.dto.AddToCartRequest;
import com.uniblox.ecommerce.model.Cart;
import com.uniblox.ecommerce.model.CartItem;
import com.uniblox.ecommerce.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CartService - Shopping cart business logic.
 * Key feature: Prevents duplicate items by updating quantity instead.
 */
@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    /**
     * Add item to cart with deduplication.
     * If product already exists → increment quantity
     * If product not exists → add new item
     */
    public void addToCart(AddToCartRequest request) {
        // Get existing cart or create new one
        Cart cart = cartRepository.findByUserId(request.getUserId());
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(request.getUserId());
        }

        // CRITICAL: Check for existing item using stream to avoid duplicates
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // UPDATE: Increment quantity instead of creating duplicate
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            // ADD: New product to cart
            CartItem newItem = new CartItem(request.getProductId(), request.getQuantity(), request.getPrice());
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId);
    }
}

