package com.uniblox.ecommerce.service;

import com.uniblox.ecommerce.AppConstants;
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
import java.util.List;

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

    @InjectMocks
    private OrderService orderService;

    private Cart testCart;
    private Discount testDiscount;

    @BeforeEach
    void setUp() {
        testCart = new Cart();
        testCart.setUserId("user1");
        testCart.getItems().add(new CartItem("prod1", 2, 10.0));

        testDiscount = new Discount();
        testDiscount.setCode("DISC10");
        testDiscount.setPercentage(10.0);
        testDiscount.setUsed(false);
        // no userId set — backward-compatible, binding check skipped
    }

    @Test
    void checkout_WithValidCart_ShouldCreateOrder() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("");

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());

        Order result = orderService.checkout(request);

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals(20.0, result.getTotalAmount());
        assertEquals(0.0, result.getDiscountApplied());
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).deleteByUserId("user1");
    }

    @Test
    void checkout_WithValidDiscountCode_ShouldApplyDiscount() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("DISC10");

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(discountRepository.findByCode("DISC10")).thenReturn(testDiscount);
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());

        Order result = orderService.checkout(request);

        assertEquals(18.0, result.getTotalAmount()); // 20 - 2
        assertEquals(2.0, result.getDiscountApplied());
        verify(discountRepository).save(testDiscount);
        assertTrue(testDiscount.isUsed());
    }

    @Test
    void checkout_WithDiscountCodeBoundToOtherUser_ShouldThrowException() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("DISC10");

        testDiscount.setUserId("user2"); // belongs to a different user
        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(discountRepository.findByCode("DISC10")).thenReturn(testDiscount);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.checkout(request)
        );
        assertEquals("Discount code does not belong to this user", ex.getMessage());
    }

    @Test
    void checkout_WithEmptyCart_ShouldThrowException() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user_empty");
        request.setDiscountCode("");

        Cart emptyCart = new Cart();
        emptyCart.setUserId("user_empty");

        when(cartRepository.findByUserId("user_empty")).thenReturn(emptyCart);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.checkout(request)
        );
        assertEquals("Cart is empty", ex.getMessage());
    }

    @Test
    void checkout_WithInvalidDiscountCode_ShouldThrowException() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("INVALID_CODE");

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(discountRepository.findByCode("INVALID_CODE")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.checkout(request)
        );
        assertEquals("Invalid or used discount code", ex.getMessage());
    }

    @Test
    void checkout_WithUsedDiscountCode_ShouldThrowException() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("USED_CODE");

        Discount usedDiscount = new Discount();
        usedDiscount.setCode("USED_CODE");
        usedDiscount.setPercentage(10.0);
        usedDiscount.setUsed(true);

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(discountRepository.findByCode("USED_CODE")).thenReturn(usedDiscount);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.checkout(request)
        );
        assertEquals("Invalid or used discount code", ex.getMessage());
    }

    @Test
    void checkout_OnNthOrder_ShouldGenerateDiscountCodeAndReturnInOrder() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("");

        List<Order> nthOrders = new ArrayList<>();
        for (int i = 0; i < AppConstants.NTH_ORDER; i++) {
            Order o = new Order();
            o.setUserId("user1");
            nthOrders.add(o);
        }

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(orderRepository.findAll()).thenReturn(nthOrders);
        when(discountRepository.saveIfAbsent(any(Discount.class))).thenReturn(true);

        Order result = orderService.checkout(request);

        verify(discountRepository).saveIfAbsent(any(Discount.class));
        assertNotNull(result.getEarnedDiscountCode());
        assertTrue(result.getEarnedDiscountCode().startsWith(AppConstants.DISCOUNT_CODE_PREFIX));
    }

    @Test
    void checkout_WhenDiscountGenerationFails_ShouldStillClearCart() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("");

        List<Order> nthOrders = new ArrayList<>();
        for (int i = 0; i < AppConstants.NTH_ORDER; i++) {
            Order o = new Order();
            o.setUserId("user1");
            nthOrders.add(o);
        }

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(orderRepository.findAll()).thenReturn(nthOrders);
        // saveIfAbsent returning false on all attempts causes generation to throw — cart must still clear
        when(discountRepository.saveIfAbsent(any(Discount.class))).thenReturn(false);

        Order result = orderService.checkout(request);

        assertNotNull(result);
        verify(cartRepository).deleteByUserId("user1");
    }

    @Test
    void checkout_BelowNthOrder_ShouldNotGenerateDiscountCode() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode("");

        List<Order> threeOrders = new ArrayList<>();
        for (int i = 0; i < AppConstants.NTH_ORDER - 1; i++) {
            Order o = new Order();
            o.setUserId("user1");
            threeOrders.add(o);
        }

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(orderRepository.findAll()).thenReturn(threeOrders);

        orderService.checkout(request);

        verify(discountRepository, never()).saveIfAbsent(any(Discount.class));
    }

    @Test
    void generateDiscountForUser_ShouldCreateCodeWithPrefix() {
        when(discountRepository.saveIfAbsent(any(Discount.class))).thenReturn(true);

        String code = orderService.generateDiscountForUser("user1");

        assertNotNull(code);
        assertTrue(code.startsWith(AppConstants.DISCOUNT_CODE_PREFIX));
        verify(discountRepository).saveIfAbsent(argThat(d ->
                d.getCode().equals(code) &&
                d.getUserId().equals("user1") &&
                d.getPercentage() == AppConstants.DISCOUNT_PERCENTAGE &&
                !d.isUsed()
        ));
    }

    @Test
    void checkout_WithNullDiscountCode_ShouldProceedWithoutDiscount() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId("user1");
        request.setDiscountCode(null);

        when(cartRepository.findByUserId("user1")).thenReturn(testCart);
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());

        Order result = orderService.checkout(request);

        assertNotNull(result);
        assertEquals(20.0, result.getTotalAmount());
        assertEquals(0.0, result.getDiscountApplied());
        verify(discountRepository, never()).findByCode(any());
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        ArrayList<Order> expectedOrders = new ArrayList<>();
        Order order = new Order();
        order.setUserId("user1");
        expectedOrders.add(order);

        when(orderRepository.findAll()).thenReturn(expectedOrders);

        List<Order> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllDiscounts_ShouldReturnAllDiscounts() {
        ArrayList<Discount> expectedDiscounts = new ArrayList<>();
        expectedDiscounts.add(testDiscount);

        when(discountRepository.findAll()).thenReturn(expectedDiscounts);

        Iterable<Discount> result = orderService.getAllDiscounts();

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
    }
}
