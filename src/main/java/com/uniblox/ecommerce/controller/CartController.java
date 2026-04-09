package com.uniblox.ecommerce.controller;
import com.uniblox.ecommerce.dto.AddToCartRequest;
import com.uniblox.ecommerce.model.Cart;
import com.uniblox.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody AddToCartRequest request) {
        cartService.addToCart(request);
        return ResponseEntity.ok("Item added to cart");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable String userId) {
        Cart cart = cartService.getCart(userId);

        if (cart == null) {
            return ResponseEntity.notFound().build();  // Return 404 Not Found
        }

        return ResponseEntity.ok(cart);  // Return 200 OK with cart data
    }

}
