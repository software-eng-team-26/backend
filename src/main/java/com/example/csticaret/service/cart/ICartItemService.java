package com.example.csticaret.service.cart;

import com.example.csticaret.model.CartItem;
import java.util.List;

public interface ICartItemService {
    void addItemToCart(Long cartId, Long productId, int quantity);
    void removeItemFromCart(Long cartId, Long productId);
    void updateItemQuantity(Long cartId, Long productId, int quantity);

    CartItem getCartItem(Long cartId, Long productId);
    List<CartItem> getCartItems(Long cartId);
}
