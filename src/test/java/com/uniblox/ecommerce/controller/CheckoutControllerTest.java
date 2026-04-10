package com.uniblox.ecommerce.controller;

import com.uniblox.ecommerce.dto.CheckoutRequest;
import com.uniblox.ecommerce.model.Order;
import com.uniblox.ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CheckoutController checkoutController;

    @Test
    void checkout_ShouldReturnOrderSuccessfully() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("");

        Order expectedOrder = new Order();
        expectedOrder.setOrderId("ORDER1");
        expectedOrder.setUserId("user1");
        expectedOrder.setItems(new ArrayList<>());
        expectedOrder.setTotalAmount(100.0);
        expectedOrder.setDiscountApplied(0.0);

        when(orderService.checkout(request)).thenReturn(expectedOrder);

        // Act
        ResponseEntity<Order> response = checkoutController.checkout(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("user1", response.getBody().getUserId());
        assertEquals(100.0, response.getBody().getTotalAmount());
        verify(orderService).checkout(request);
    }

    @Test
    void checkout_ShouldReturnOrderWithDiscount() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("DISC10");

        Order expectedOrder = new Order();
        expectedOrder.setOrderId("ORDER1");
        expectedOrder.setUserId("user1");
        expectedOrder.setItems(new ArrayList<>());
        expectedOrder.setTotalAmount(90.0);
        expectedOrder.setDiscountApplied(10.0);

        when(orderService.checkout(request)).thenReturn(expectedOrder);

        // Act
        ResponseEntity<Order> response = checkoutController.checkout(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(90.0, response.getBody().getTotalAmount());
        assertEquals(10.0, response.getBody().getDiscountApplied());
    }

    @Test
    void checkout_ShouldHandleEmptyCart() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user_empty_cart");
        request.setDiscountCode("");

        when(orderService.checkout(request)).thenThrow(
                new IllegalArgumentException("Cart is empty")
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutController.checkout(request)
        );
        assertEquals("Cart is empty", exception.getMessage());
    }

    @Test
    void checkout_ShouldHandleInvalidDiscountCode() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("INVALID_CODE");

        when(orderService.checkout(request)).thenThrow(
                new IllegalArgumentException("Invalid or used discount code")
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutController.checkout(request)
        );
        assertEquals("Invalid or used discount code", exception.getMessage());
    }
}

