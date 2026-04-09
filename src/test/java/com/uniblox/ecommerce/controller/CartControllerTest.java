package com.uniblox.ecommerce.controller;

import com.uniblox.ecommerce.dto.AddToCartRequest;
import com.uniblox.ecommerce.model.Cart;
import com.uniblox.ecommerce.model.CartItem;
import com.uniblox.ecommerce.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Test
    void addToCart_ShouldReturnSuccessResponse() {
        // Arrange
        AddToCartRequest request = new AddToCartRequest();
        request.setUserId("user1");
        request.setProductId("prod1");
        request.setQuantity(2);
        request.setPrice(10.0);

        // Act
        ResponseEntity<String> response = cartController.addToCart(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Item added to cart", response.getBody());
        verify(cartService).addToCart(request);
    }

    @Test
    void getCart_ExistingCart_ShouldReturnCart() {
        // Arrange
        Cart cart = new Cart();
        cart.setUserId("user1");
        cart.getItems().add(new CartItem("prod1", 2, 10.0));

        when(cartService.getCart("user1")).thenReturn(cart);

        // Act
        ResponseEntity<Cart> response = cartController.getCart("user1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("user1", response.getBody().getUserId());
        assertEquals(1, response.getBody().getItems().size());
        verify(cartService).getCart("user1");
    }

    @Test
    void getCart_NonExistingCart_ShouldReturn404() {
        // Arrange
        when(cartService.getCart("nonexistent")).thenReturn(null);

        // Act
        ResponseEntity<Cart> response = cartController.getCart("nonexistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(cartService).getCart("nonexistent");
    }
}
