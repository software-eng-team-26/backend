package com.example.csticaret.controller;

import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.Cart;
import com.example.csticaret.model.User;
import com.example.csticaret.response.ApiResponse;
import com.example.csticaret.service.cart.ICartService;
import com.example.csticaret.service.cart.ICartItemService;
import com.example.csticaret.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
    private final ICartService cartService;
    private final ICartItemService cartItemService;
    private final IUserService userService;
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @GetMapping("/my-cart")
    public ResponseEntity<ApiResponse> getCurrentUserCart(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Getting cart for user: {}", userDetails.getUsername());
            User user = userService.getUserByEmail(userDetails.getUsername());
            Cart cart = cartService.getCartByUserId(user.getId());
            
            log.info("Cart found: {}", cart.getId());
            log.info("Cart items count: {}", cart.getItems().size());
            
            // Create a simplified response
            Map<String, Object> cartResponse = new HashMap<>();
            cartResponse.put("id", cart.getId());
            cartResponse.put("userId", cart.getUserId());
            cartResponse.put("items", cart.getItems());
            cartResponse.put("totalAmount", cart.getTotalAmount());
            
            return ResponseEntity.ok(new ApiResponse("Success", cartResponse));
        } catch (ResourceNotFoundException e) {
            log.error("Cart not found for user: {}", userDetails.getUsername());
            return ResponseEntity.ok(new ApiResponse("New cart created", 
                cartService.getCartByUserId(userService.getUserByEmail(userDetails.getUsername()).getId())));
        }
    }

    @PostMapping("/add-item")
    public ResponseEntity<ApiResponse> addItemToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        log.info("Adding item to cart. User: {}, Product: {}, Quantity: {}", 
            userDetails.getUsername(), productId, quantity);
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            log.info("Found user: {}", user.getId());
            
            Cart cart = cartService.getCartByUserId(user.getId());
            log.info("Found/Created cart: {}", cart.getId());
            
            cartItemService.addItemToCart(cart.getId(), productId, quantity);
            log.info("Added item to cart");
            
            // Get updated cart to return
            Cart updatedCart = cartService.getCartByUserId(user.getId());
            log.info("Updated cart items count: {}", updatedCart.getItems().size());
            log.info("Updated cart total: {}", updatedCart.getTotalAmount());
            
            // Create a simplified response
            Map<String, Object> cartResponse = new HashMap<>();
            cartResponse.put("id", updatedCart.getId());
            cartResponse.put("userId", updatedCart.getUserId());
            cartResponse.put("items", updatedCart.getItems());
            cartResponse.put("totalAmount", updatedCart.getTotalAmount());
            
            return ResponseEntity.ok(new ApiResponse("Item added to cart successfully", cartResponse));
        } catch (ResourceNotFoundException e) {
            log.error("Error adding item to cart:", e);
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            Cart cart = cartService.getCartByUserId(user.getId());
            cartService.clearCart(cart.getId());
            
            // Create empty cart response
            Map<String, Object> cartResponse = new HashMap<>();
            cartResponse.put("id", cart.getId());
            cartResponse.put("userId", cart.getUserId());
            cartResponse.put("items", new HashSet<>());
            cartResponse.put("totalAmount", BigDecimal.ZERO);
            
            return ResponseEntity.ok(new ApiResponse("Cart cleared successfully", cartResponse));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse> getTotalAmount(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            Cart cart = cartService.getCartByUserId(user.getId());
            BigDecimal totalPrice = cartService.getTotalPrice(cart.getId());
            return ResponseEntity.ok(new ApiResponse("Total Price", totalPrice));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<ApiResponse> removeItemFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            Cart cart = cartService.getCartByUserId(user.getId());
            
            if (cart == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Cart not found", null));
            }

            // Verify cart ownership
            if (!cart.getId().equals(cartId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("You don't have permission to modify this cart", null));
            }

            cartItemService.removeItemFromCart(cartId, productId);
            
            // Get updated cart
            Cart updatedCart = cartService.getCartByUserId(user.getId());
            return ResponseEntity.ok(new ApiResponse("Item removed successfully", updatedCart));
        } catch (Exception e) {
            log.error("Error removing item from cart:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("Error removing item from cart", null));
        }
    }
}
