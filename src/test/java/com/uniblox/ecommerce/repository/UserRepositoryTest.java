package com.uniblox.ecommerce.repository;

import com.uniblox.ecommerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
    }

    @Test
    void findByUserId_NewUser_ShouldCreateWithZeroOrders() {
        // Act
        User result = userRepository.findByUserId("user1");

        // Assert
        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals(0, result.getOrderCount());
    }

    @Test
    void save_UpdateUser_ShouldPersistOrderCount() {
        // Arrange
        User user = new User();
        user.setUserId("user1");
        user.setOrderCount(1);

        // Act
        userRepository.save(user);
        user.setOrderCount(5);
        userRepository.save(user);

        // Assert
        User updated = userRepository.findByUserId("user1");
        assertEquals(5, updated.getOrderCount());
    }

    @Test
    void findByUserId_ExistingUser_ShouldReturnSavedUser() {
        // Arrange
        User user = new User();
        user.setUserId("user1");
        user.setOrderCount(3);
        userRepository.save(user);

        // Act
        User result = userRepository.findByUserId("user1");

        // Assert
        assertEquals(3, result.getOrderCount());
    }
}

