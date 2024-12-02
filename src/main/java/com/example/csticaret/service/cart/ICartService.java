package com.example.csticaret.service.cart;

import com.example.csticaret.model.Cart;
import java.math.BigDecimal;

public interface ICartService {
    Cart getCart(Long id);
    Cart getCartByUserId(Long userId);
    void clearCart(Long cartId);
    BigDecimal getTotalPrice(Long id);
    Long initializeNewCart(Long userId);
    void addItemToCart(Long cartId, Long productId, int quantity);
    void removeItemFromCart(Long cartId, Long productId);
    Cart getOrCreateGuestCart(String guestId);
}
