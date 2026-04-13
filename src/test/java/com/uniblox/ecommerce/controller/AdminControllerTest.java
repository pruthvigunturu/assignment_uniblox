package com.uniblox.ecommerce.controller;

import com.uniblox.ecommerce.model.CartItem;
import com.uniblox.ecommerce.model.Discount;
import com.uniblox.ecommerce.model.Order;
import com.uniblox.ecommerce.model.User;
import com.uniblox.ecommerce.repository.UserRepository;
import com.uniblox.ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AdminControllerTest - Comprehensive tests for admin endpoints
 *
 * Tests cover:
 * 1. GET /api/admin/stats - Statistics aggregation
 * 2. POST /api/admin/generate-discount - Discount generation eligibility
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminController adminController;

    // ========== GET /api/admin/stats Tests ==========

    @Test
    void getStats_WithNoOrders_ShouldReturnZeroMetrics() {
        // Arrange
        when(orderService.getAllOrders()).thenReturn(new ArrayList<>());
        when(orderService.getAllDiscounts()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().get("totalItemsPurchased"));
        assertEquals(0.0, response.getBody().get("totalRevenue"));
        assertEquals(0L, response.getBody().get("totalDiscountCodes"));
        assertEquals(0.0, response.getBody().get("totalDiscountsGiven"));
        verify(orderService).getAllOrders();
        verify(orderService).getAllDiscounts();
    }

    @Test
    void getStats_WithSingleOrder_ShouldCalculateCorrectMetrics() {
        // Arrange - Create single order with 2 items
        Order order = new Order();
        order.setUserId("user1");
        order.setTotalAmount(100.0);
        order.setDiscountApplied(10.0);

        CartItem item1 = new CartItem("prod1", 2, 25.0);
        CartItem item2 = new CartItem("prod2", 3, 25.0);
        order.setItems(Arrays.asList(item1, item2));

        when(orderService.getAllOrders()).thenReturn(Collections.singletonList(order));
        when(orderService.getAllDiscounts()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().get("totalItemsPurchased")); // 2 + 3
        assertEquals(100.0, response.getBody().get("totalRevenue"));
        assertEquals(0L, response.getBody().get("totalDiscountCodes"));
        assertEquals(10.0, response.getBody().get("totalDiscountsGiven"));
    }

    @Test
    void getStats_WithMultipleOrders_ShouldAggregateAllMetrics() {
        // Arrange - Create multiple orders
        Order order1 = new Order();
        order1.setUserId("user1");
        order1.setTotalAmount(50.0);
        order1.setDiscountApplied(5.0);
        order1.setItems(Arrays.asList(new CartItem("prod1", 2, 25.0)));

        Order order2 = new Order();
        order2.setUserId("user2");
        order2.setTotalAmount(75.0);
        order2.setDiscountApplied(7.5);
        order2.setItems(Arrays.asList(new CartItem("prod2", 3, 25.0)));

        List<Order> orders = Arrays.asList(order1, order2);
        List<Discount> discounts = Arrays.asList(
            createDiscount("DISC1", 10.0, true),
            createDiscount("DISC2", 10.0, false)
        );

        when(orderService.getAllOrders()).thenReturn(orders);
        when(orderService.getAllDiscounts()).thenReturn(discounts);

        // Act
        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().get("totalItemsPurchased")); // 2 + 3
        assertEquals(125.0, response.getBody().get("totalRevenue")); // 50 + 75
        assertEquals(2L, response.getBody().get("totalDiscountCodes"));
        assertEquals(12.5, response.getBody().get("totalDiscountsGiven")); // 5 + 7.5
    }

    @Test
    void getStats_WithManyDiscounts_ShouldCountAllCodes() {
        // Arrange - Create many discount codes
        List<Discount> discounts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            discounts.add(createDiscount("DISC" + i, 10.0, i % 2 == 0));
        }

        when(orderService.getAllOrders()).thenReturn(new ArrayList<>());
        when(orderService.getAllDiscounts()).thenReturn(discounts);

        // Act
        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        // Assert
        assertEquals(10L, response.getBody().get("totalDiscountCodes"));
    }

    @Test
    void getStats_ShouldReturnMapWithFourKeys() {
        // Arrange
        when(orderService.getAllOrders()).thenReturn(new ArrayList<>());
        when(orderService.getAllDiscounts()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        // Assert
        Map<String, Object> stats = response.getBody();
        assertEquals(4, stats.size());
        assertTrue(stats.containsKey("totalItemsPurchased"));
        assertTrue(stats.containsKey("totalRevenue"));
        assertTrue(stats.containsKey("totalDiscountCodes"));
        assertTrue(stats.containsKey("totalDiscountsGiven"));
    }

    @Test
    void getStats_WithComplexOrderStructure_ShouldHandleMultipleItems() {
        // Arrange - Create order with 5 different items
        Order order = new Order();
        order.setUserId("user1");
        order.setTotalAmount(200.0);
        order.setDiscountApplied(0.0);
        order.setItems(Arrays.asList(
            new CartItem("prod1", 1, 50.0),
            new CartItem("prod2", 2, 40.0),
            new CartItem("prod3", 3, 10.0),
            new CartItem("prod4", 4, 0.0),
            new CartItem("prod5", 5, 0.0)
        ));

        when(orderService.getAllOrders()).thenReturn(Collections.singletonList(order));
        when(orderService.getAllDiscounts()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        // Assert - Should sum all quantities: 1+2+3+4+5 = 15
        assertEquals(15, response.getBody().get("totalItemsPurchased"));
    }

    // ========== POST /api/admin/generate-discount Tests ==========

    @Test
    void generateDiscount_UserEligible_ShouldReturnSuccess() {
        // Arrange - User with 5 orders (eligible)
        User user = new User();
        user.setUserId("user1");
        user.setOrderCount(5);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", "user1");

        when(userRepository.findByUserId("user1")).thenReturn(user);

        // Act
        ResponseEntity<String> response = adminController.generateDiscount(requestBody);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Discount generated for user user1", response.getBody());
        verify(orderService).generateDiscountForUser("user1");
    }

    @Test
    void generateDiscount_UserWith10Orders_ShouldReturnSuccess() {
        // Arrange - User with 10 orders (2x5, eligible)
        User user = new User();
        user.setUserId("user1");
        user.setOrderCount(10);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", "user1");

        when(userRepository.findByUserId("user1")).thenReturn(user);

        // Act
        ResponseEntity<String> response = adminController.generateDiscount(requestBody);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).generateDiscountForUser("user1");
    }

    @Test
    void generateDiscount_UserNotEligible_ShouldReturnBadRequest() {
        // Arrange - User with 3 orders (not eligible)
        User user = new User();
        user.setUserId("user1");
        user.setOrderCount(3);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", "user1");

        when(userRepository.findByUserId("user1")).thenReturn(user);

        // Act
        ResponseEntity<String> response = adminController.generateDiscount(requestBody);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not eligible for discount", response.getBody());
        verify(orderService, never()).generateDiscountForUser("user1");
    }

    @Test
    void generateDiscount_UserWithZeroOrders_ShouldReturnBadRequest() {
        // Arrange - User with 0 orders (not eligible)
        User user = new User();
        user.setUserId("user1");
        user.setOrderCount(0);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", "user1");

        when(userRepository.findByUserId("user1")).thenReturn(user);

        // Act
        ResponseEntity<String> response = adminController.generateDiscount(requestBody);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService, never()).generateDiscountForUser("user1");
    }

    @Test
    void generateDiscount_UserWith4Orders_ShouldReturnBadRequest() {
        // Arrange - User with 4 orders (not 5x)
        User user = new User();
        user.setUserId("user1");
        user.setOrderCount(4);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", "user1");

        when(userRepository.findByUserId("user1")).thenReturn(user);

        // Act
        ResponseEntity<String> response = adminController.generateDiscount(requestBody);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService, never()).generateDiscountForUser("user1");
    }

    @Test
    void generateDiscount_DifferentUsersEligible_ShouldSucceedForBoth() {
        // Arrange - Create two eligible users
        User user1 = new User();
        user1.setUserId("user1");
        user1.setOrderCount(5);

        User user2 = new User();
        user2.setUserId("user2");
        user2.setOrderCount(10);

        when(userRepository.findByUserId("user1")).thenReturn(user1);
        when(userRepository.findByUserId("user2")).thenReturn(user2);

        // Act & Assert for user1
        Map<String, String> body1 = new HashMap<>();
        body1.put("userId", "user1");
        ResponseEntity<String> response1 = adminController.generateDiscount(body1);

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals("Discount generated for user user1", response1.getBody());

        // Act & Assert for user2
        Map<String, String> body2 = new HashMap<>();
        body2.put("userId", "user2");
        ResponseEntity<String> response2 = adminController.generateDiscount(body2);

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals("Discount generated for user user2", response2.getBody());

        // Verify both were processed
        verify(orderService).generateDiscountForUser("user1");
        verify(orderService).generateDiscountForUser("user2");
    }

    @Test
    void generateDiscount_ShouldCallGenerateDiscountServiceMethod() {
        // Arrange
        User user = new User();
        user.setUserId("testUser");
        user.setOrderCount(15);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", "testUser");

        when(userRepository.findByUserId("testUser")).thenReturn(user);

        // Act
        adminController.generateDiscount(requestBody);

        // Assert - Verify service method was called exactly once with correct userId
        verify(orderService, times(1)).generateDiscountForUser("testUser");
    }

    // ========== Helper Methods ==========

    /**
     * Helper to create discount objects for testing
     */
    private Discount createDiscount(String code, double percentage, boolean used) {
        Discount discount = new Discount();
        discount.setCode(code);
        discount.setPercentage(percentage);
        discount.setUsed(used);
        return discount;
    }
}

