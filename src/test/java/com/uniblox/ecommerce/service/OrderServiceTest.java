package com.uniblox.ecommerce.service;

import com.uniblox.ecommerce.dto.CheckoutRequest;
import com.uniblox.ecommerce.model.*;
import com.uniblox.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private Cart testCart;
    private User testUser;
    private Discount testDiscount;

    @BeforeEach
    void setUp() {
        testCart = new Cart();
        testCart.setUserId("user1");
        testCart.getItems().add(new CartItem("prod1", 2, 10.0));

        testUser = new User();
        testUser.setUserId("user1");
        testUser.setOrderCount(0);

        testDiscount = new Discount();
        testDiscount.setCode("DISC10");
        testDiscount.setPercentage(10.0);
        testDiscount.setUsed(false);
    }

    @Test
    void checkout_WithValidCart_ShouldCreateOrder() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("");

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(userRepository.findByUserId("user1")).thenReturn(testUser);

        // Act
        Order result = orderService.checkout(request);

        // Assert
        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals(20.0, result.getTotalAmount());
        assertEquals(0.0, result.getDiscountApplied());
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).deleteByUserId("user1");
    }

    @Test
    void checkout_WithValidDiscountCode_ShouldApplyDiscount() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("DISC10");

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(discountRepository.findByCode("DISC10")).thenReturn(testDiscount);
        when(userRepository.findByUserId("user1")).thenReturn(testUser);

        // Act
        Order result = orderService.checkout(request);

        // Assert
        assertEquals(18.0, result.getTotalAmount()); // 20 - 2
        assertEquals(2.0, result.getDiscountApplied());
        verify(discountRepository).save(testDiscount);
        assertTrue(testDiscount.isUsed());
    }

    @Test
    void checkout_WithEmptyCart_ShouldThrowException() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user_empty");
        request.setDiscountCode("");

        Cart emptyCart = new Cart();
        emptyCart.setUserId("user_empty");

        when(cartRepository.findByUserId("user_empty")).thenReturn(emptyCart);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.checkout(request)
        );
        assertEquals("Cart is empty", exception.getMessage());
    }

    @Test
    void checkout_WithInvalidDiscountCode_ShouldThrowException() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("INVALID_CODE");

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(discountRepository.findByCode("INVALID_CODE")).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.checkout(request)
        );
        assertEquals("Invalid or used discount code", exception.getMessage());
    }

    @Test
    void checkout_WithUsedDiscountCode_ShouldThrowException() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("USED_CODE");

        Discount usedDiscount = new Discount();
        usedDiscount.setCode("USED_CODE");
        usedDiscount.setPercentage(10.0);
        usedDiscount.setUsed(true);

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(discountRepository.findByCode("USED_CODE")).thenReturn(usedDiscount);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.checkout(request)
        );
        assertEquals("Invalid or used discount code", exception.getMessage());
    }

    @Test
    void checkout_ShouldUpdateUserOrderCount() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("");

        testUser.setOrderCount(1);

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(userRepository.findByUserId("user1")).thenReturn(testUser);

        // Act
        orderService.checkout(request);

        // Assert
        assertEquals(2, testUser.getOrderCount());
        verify(userRepository).save(testUser);
    }

    @Test
    void checkout_OnFifthOrder_ShouldGenerateDiscountCode() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("");

        testUser.setOrderCount(4); // Next order will be 5th

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(userRepository.findByUserId("user1")).thenReturn(testUser);

        // Act
        orderService.checkout(request);

        // Assert
        assertEquals(5, testUser.getOrderCount());
        verify(discountRepository).save(any(Discount.class));
    }

    @Test
    void generateDiscountForUser_ShouldCreateDiscountCode() {
        // Act
        orderService.generateDiscountForUser("user1");

        // Assert
        verify(discountRepository).save(any(Discount.class));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        ArrayList<Order> expectedOrders = new ArrayList<>();
        Order order = new Order();
        order.setUserId("user1");
        expectedOrders.add(order);

        when(orderRepository.findAll()).thenReturn(expectedOrders);

        // Act
        java.util.List<Order> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getAllDiscounts_ShouldReturnAllDiscounts() {
        // Arrange
        ArrayList<Discount> expectedDiscounts = new ArrayList<>();
        expectedDiscounts.add(testDiscount);

        when(discountRepository.findAll()).thenReturn(expectedDiscounts);

        // Act
        Iterable<Discount> result = orderService.getAllDiscounts();

        // Assert
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(discountRepository).findAll();
    }

    @Test
    void checkout_WithNullDiscountCode_ShouldProceedWithoutDiscount() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode(null);

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(userRepository.findByUserId("user1")).thenReturn(testUser);

        // Act
        Order result = orderService.checkout(request);

        // Assert
        assertNotNull(result);
        assertEquals(20.0, result.getTotalAmount());
        assertEquals(0.0, result.getDiscountApplied());
        verify(discountRepository, never()).findByCode(any());
    }
}

