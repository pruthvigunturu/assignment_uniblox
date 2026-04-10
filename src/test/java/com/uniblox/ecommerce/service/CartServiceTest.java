package com.uniblox.ecommerce.service;

import com.uniblox.ecommerce.dto.AddToCartRequest;
import com.uniblox.ecommerce.model.Cart;
import com.uniblox.ecommerce.model.CartItem;
import com.uniblox.ecommerce.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCart_NewCart_ShouldCreateAndAddItem() {
        // Arrange
        AddToCartRequest request = new AddToCartRequest();
        request.setUserId("user1");
        request.setProductId("prod1");
        request.setQuantity(2);
        request.setPrice(10.0);

        when(cartRepository.findByUserId("user1")).thenReturn(null);

        // Act
        cartService.addToCart(request);

        // Assert
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addToCart_ExistingItem_ShouldUpdateQuantity() {
        // Arrange
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

        // Act
        cartService.addToCart(request);

        // Assert
        assertEquals(3, item.getQuantity()); // 1 + 2
        assertEquals(1, existingCart.getItems().size()); // No duplicate
        verify(cartRepository).save(existingCart);
    }

    @Test
    void addToCart_NewItemToExistingCart_ShouldAddNewItem() {
        // Arrange
        Cart existingCart = new Cart();
        existingCart.setUserId("user1");
        existingCart.getItems().add(new CartItem("prod1", 1, 10.0));

        AddToCartRequest request = new AddToCartRequest();
        request.setUserId("user1");
        request.setProductId("prod2");
        request.setQuantity(3);
        request.setPrice(20.0);

        when(cartRepository.findByUserId("user1")).thenReturn(existingCart);

        // Act
        cartService.addToCart(request);

        // Assert
        assertEquals(2, existingCart.getItems().size());
        verify(cartRepository).save(existingCart);
    }

    @Test
    void getCart_ExistingCart_ShouldReturnCart() {
        // Arrange
        Cart cart = new Cart();
        cart.setUserId("user1");
        when(cartRepository.findByUserId("user1")).thenReturn(cart);

        // Act
        Cart result = cartService.getCart("user1");

        // Assert
        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        verify(cartRepository).findByUserId("user1");
    }

    @Test
    void getCart_NonExistingCart_ShouldReturnNull() {
        // Arrange
        when(cartRepository.findByUserId("nonexistent")).thenReturn(null);

        // Act
        Cart result = cartService.getCart("nonexistent");

        // Assert
        assertNull(result);
    }
}