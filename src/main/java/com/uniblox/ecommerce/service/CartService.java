package com.uniblox.ecommerce.service;

import com.uniblox.ecommerce.dto.AddToCartRequest;
import com.uniblox.ecommerce.model.Cart;
import com.uniblox.ecommerce.model.CartItem;
import com.uniblox.ecommerce.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public void addToCart(AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUserId());
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(request.getUserId());
        }

        // Check if item already exists, update quantity
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = new CartItem(request.getProductId(), request.getQuantity(), request.getPrice());
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId);
    }
}
