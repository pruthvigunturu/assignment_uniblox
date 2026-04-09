package com.uniblox.ecommerce.service;

import com.uniblox.ecommerce.dto.AddToCartRequest;
import com.uniblox.ecommerce.model.Cart;
import com.uniblox.ecommerce.model.CartItem;
import com.uniblox.ecommerce.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddToCart_NewCart() {
        AddToCartRequest request = new AddToCartRequest();
        request.setUserId("user1");
        request.setProductId("prod1");
        request.setQuantity(2);
        request.setPrice(10.0);

        when(cartRepository.findByUserId("user1")).thenReturn(null);

        cartService.addToCart(request);

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testAddToCart_ExistingItem() {
        Cart existingCart = new Cart();
        existingCart.setUserId("user1");
        CartItem item = new CartItem("prod1", 1, 10.0);
        existingCart.getItems().add(item);

        AddToCartRequest request = new AddToCartRequest();
        request.setUserId("user1");
        request.setProductId("prod1");
        request.setQuantity(2);
        request.setPrice(10.0);

        when(cartRepository.findByUserId("user1")).thenReturn(existingCart);

        cartService.addToCart(request);

        assertEquals(3, item.getQuantity());
        verify(cartRepository).save(existingCart);
    }

    @Test
    void testGetCart() {
        Cart cart = new Cart();
        cart.setUserId("user1");

        when(cartRepository.findByUserId("user1")).thenReturn(cart);

        Cart result = cartService.getCart("user1");

        assertEquals(cart, result);
    }
}