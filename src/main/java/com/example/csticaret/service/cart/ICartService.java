package com.example.csticaret.service.cart;

import com.example.csticaret.model.Cart;

import java.math.BigDecimal;

public interface ICartService {
    Cart getCart(Long id);
    void clearCart(Long id);
    BigDecimal getTotalPrice(Long id);

    Long initializeNewCart(Long userId);

    Cart getCartByUserId(Long userId);
}
