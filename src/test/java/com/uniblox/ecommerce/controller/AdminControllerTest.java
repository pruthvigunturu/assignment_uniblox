package com.uniblox.ecommerce.controller;

import com.uniblox.ecommerce.AppConstants;
import com.uniblox.ecommerce.model.CartItem;
import com.uniblox.ecommerce.model.Discount;
import com.uniblox.ecommerce.model.Order;
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

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private AdminController adminController;

    // ========== GET /api/admin/stats Tests ==========

    @Test
    void getStats_WithNoOrders_ShouldReturnZeroMetrics() {
        when(orderService.getAllOrders()).thenReturn(new ArrayList<>());
        when(orderService.getAllDiscounts()).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().get("totalItemsPurchased"));
        assertEquals(0.0, response.getBody().get("totalRevenue"));
        assertEquals(0.0, response.getBody().get("grossRevenue"));
        assertEquals(0L, response.getBody().get("totalDiscountCodes"));
        assertEquals(0.0, response.getBody().get("totalDiscountsGiven"));
    }

    @Test
    void getStats_WithSingleOrder_ShouldCalculateCorrectMetrics() {
        Order order = new Order();
        order.setUserId("user1");
        order.setTotalAmount(100.0);
        order.setDiscountApplied(10.0);
        order.setItems(Arrays.asList(new CartItem("prod1", 2, 25.0), new CartItem("prod2", 3, 25.0)));

        when(orderService.getAllOrders()).thenReturn(Collections.singletonList(order));
        when(orderService.getAllDiscounts()).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().get("totalItemsPurchased")); // 2 + 3
        assertEquals(100.0, response.getBody().get("totalRevenue"));
        assertEquals(110.0, response.getBody().get("grossRevenue")); // 100 + 10
        assertEquals(0L, response.getBody().get("totalDiscountCodes"));
        assertEquals(10.0, response.getBody().get("totalDiscountsGiven"));
    }

    @Test
    void getStats_WithMultipleOrders_ShouldAggregateAllMetrics() {
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

        List<Discount> discounts = Arrays.asList(
                createDiscount("DISC1", 10.0, true),
                createDiscount("DISC2", 10.0, false));

        when(orderService.getAllOrders()).thenReturn(Arrays.asList(order1, order2));
        when(orderService.getAllDiscounts()).thenReturn(discounts);

        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        assertEquals(5, response.getBody().get("totalItemsPurchased")); // 2 + 3
        assertEquals(125.0, response.getBody().get("totalRevenue")); // 50 + 75
        assertEquals(137.5, response.getBody().get("grossRevenue")); // 125 + 12.5
        assertEquals(2L, response.getBody().get("totalDiscountCodes"));
        assertEquals(12.5, response.getBody().get("totalDiscountsGiven")); // 5 + 7.5
    }

    @Test
    void getStats_WithManyDiscounts_ShouldCountAllCodes() {
        List<Discount> discounts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            discounts.add(createDiscount("DISC" + i, 10.0, i % 2 == 0));
        }

        when(orderService.getAllOrders()).thenReturn(new ArrayList<>());
        when(orderService.getAllDiscounts()).thenReturn(discounts);

        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        assertEquals(10L, response.getBody().get("totalDiscountCodes"));
    }

    @Test
    void getStats_ShouldReturnMapWithFiveKeys() {
        when(orderService.getAllOrders()).thenReturn(new ArrayList<>());
        when(orderService.getAllDiscounts()).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        Map<String, Object> stats = response.getBody();
        assertEquals(5, stats.size());
        assertTrue(stats.containsKey("totalItemsPurchased"));
        assertTrue(stats.containsKey("totalRevenue"));
        assertTrue(stats.containsKey("grossRevenue"));
        assertTrue(stats.containsKey("totalDiscountCodes"));
        assertTrue(stats.containsKey("totalDiscountsGiven"));
    }

    @Test
    void getStats_WithComplexOrderStructure_ShouldHandleMultipleItems() {
        Order order = new Order();
        order.setUserId("user1");
        order.setTotalAmount(200.0);
        order.setDiscountApplied(0.0);
        order.setItems(Arrays.asList(
                new CartItem("prod1", 1, 50.0),
                new CartItem("prod2", 2, 40.0),
                new CartItem("prod3", 3, 10.0),
                new CartItem("prod4", 4, 0.0),
                new CartItem("prod5", 5, 0.0)));

        when(orderService.getAllOrders()).thenReturn(Collections.singletonList(order));
        when(orderService.getAllDiscounts()).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response = adminController.getStats();

        assertEquals(15, response.getBody().get("totalItemsPurchased")); // 1+2+3+4+5
    }

    // ========== POST /api/admin/generate-discount Tests ==========

    @Test
    void generateDiscount_UserEligible_ShouldReturnSuccess() {
        Map<String, String> body = new HashMap<>();
        body.put("userId", "user1");

        when(orderService.getOrderCountForUser("user1")).thenReturn((long) AppConstants.NTH_ORDER);

        ResponseEntity<String> response = adminController.generateDiscount(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Discount generated for user user1", response.getBody());
        verify(orderService).generateDiscountForUser("user1");
    }

    @Test
    void generateDiscount_UserWith10Orders_ShouldReturnSuccess() {
        Map<String, String> body = new HashMap<>();
        body.put("userId", "user1");

        when(orderService.getOrderCountForUser("user1")).thenReturn((long) AppConstants.NTH_ORDER * 2);

        ResponseEntity<String> response = adminController.generateDiscount(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).generateDiscountForUser("user1");
    }

    @Test
    void generateDiscount_UserNotEligible_ShouldReturnBadRequest() {
        Map<String, String> body = new HashMap<>();
        body.put("userId", "user1");

        when(orderService.getOrderCountForUser("user1")).thenReturn(3L);

        ResponseEntity<String> response = adminController.generateDiscount(body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not eligible for discount", response.getBody());
        verify(orderService, never()).generateDiscountForUser("user1");
    }

    @Test
    void generateDiscount_UserWithZeroOrders_ShouldReturnBadRequest() {
        Map<String, String> body = new HashMap<>();
        body.put("userId", "user1");

        when(orderService.getOrderCountForUser("user1")).thenReturn(0L);

        ResponseEntity<String> response = adminController.generateDiscount(body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService, never()).generateDiscountForUser("user1");
    }

    @Test
    void generateDiscount_UserWith4Orders_ShouldReturnBadRequest() {
        Map<String, String> body = new HashMap<>();
        body.put("userId", "user1");

        when(orderService.getOrderCountForUser("user1")).thenReturn((long) AppConstants.NTH_ORDER - 1);

        ResponseEntity<String> response = adminController.generateDiscount(body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService, never()).generateDiscountForUser("user1");
    }

    @Test
    void generateDiscount_DifferentUsersEligible_ShouldSucceedForBoth() {
        when(orderService.getOrderCountForUser("user1")).thenReturn((long) AppConstants.NTH_ORDER);
        when(orderService.getOrderCountForUser("user2")).thenReturn((long) AppConstants.NTH_ORDER * 2);

        Map<String, String> body1 = new HashMap<>();
        body1.put("userId", "user1");
        ResponseEntity<String> response1 = adminController.generateDiscount(body1);
        assertEquals(HttpStatus.OK, response1.getStatusCode());

        Map<String, String> body2 = new HashMap<>();
        body2.put("userId", "user2");
        ResponseEntity<String> response2 = adminController.generateDiscount(body2);
        assertEquals(HttpStatus.OK, response2.getStatusCode());

        verify(orderService).generateDiscountForUser("user1");
        verify(orderService).generateDiscountForUser("user2");
    }

    @Test
    void generateDiscount_ShouldCallGenerateDiscountServiceMethod() {
        Map<String, String> body = new HashMap<>();
        body.put("userId", "testUser");

        when(orderService.getOrderCountForUser("testUser")).thenReturn((long) AppConstants.NTH_ORDER * 3);

        adminController.generateDiscount(body);

        verify(orderService, times(1)).generateDiscountForUser("testUser");
    }

    // ========== Helper Methods ==========

    private Discount createDiscount(String code, double percentage, boolean used) {
        Discount discount = new Discount();
        discount.setCode(code);
        discount.setPercentage(percentage);
        discount.setUsed(used);
        return discount;
    }
}
