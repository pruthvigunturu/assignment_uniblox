package com.uniblox.ecommerce.repository;

import com.uniblox.ecommerce.model.Cart;
import com.uniblox.ecommerce.model.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartRepositoryTest {

    private CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        cartRepository = new CartRepository();
    }

    @Test
    void save_NewCart_ShouldPersistAndRetrieve() {
        // Arrange
        Cart cart = new Cart();
        cart.setUserId("user1");
        cart.getItems().add(new CartItem("prod1", 2, 10.0));

        // Act
        cartRepository.save(cart);

        // Assert
        Cart saved = cartRepository.findByUserId("user1");
        assertNotNull(saved);
        assertEquals(1, saved.getItems().size());
    }

    @Test
    void deleteByUserId_ShouldRemoveCart() {
        // Arrange
        Cart cart = new Cart();
        cart.setUserId("user1");
        cartRepository.save(cart);

        // Act
        cartRepository.deleteByUserId("user1");

        // Assert
        assertNull(cartRepository.findByUserId("user1"));
    }

    @Test
    void findByUserId_NonExistingCart_ShouldReturnNull() {
        // Act & Assert
        assertNull(cartRepository.findByUserId("nonexistent"));
    }
}

