package com.example.csticaret.service.cart;

import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.Cart;
import com.example.csticaret.model.User;
import com.example.csticaret.repository.CartItemRepository;
import com.example.csticaret.repository.CartRepository;
import com.example.csticaret.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService{
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public Cart getCart(Long id) {
        return cartRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    @Override
    @Transactional
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUser_Id(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setTotalAmount(BigDecimal.ZERO);
                newCart.setItems(new HashSet<>());
                
                return cartRepository.save(newCart);
            });
    }

    @Override
    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = getCart(cartId);
        cartItemRepository.deleteAllByCart_Id(cartId);
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Override
    public BigDecimal getTotalPrice(Long id) {
        Cart cart = getCart(id);
        return cart.getTotalAmount();
    }

    @Override
    public Long initializeNewCart(Long userId) {
        Cart newCart = new Cart();
        newCart.setUser(userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found")));
        newCart.setTotalAmount(BigDecimal.ZERO);
        newCart.setItems(new HashSet<>());
        return cartRepository.save(newCart).getId();
    }

    @Override
    @Transactional
    public void addItemToCart(Long cartId, Long productId, int quantity) {
        Cart cart = getCart(cartId);
        // Add item logic here
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long cartId, Long productId) {
        Cart cart = getCart(cartId);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cart.updateTotalAmount();
        cartRepository.save(cart);
    }

    @Override
    public Cart getOrCreateGuestCart(String guestId) {
        return cartRepository.findByGuestId(guestId)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setGuestId(guestId);
                newCart.setTotalAmount(BigDecimal.ZERO);
                newCart.setItems(new HashSet<>());
                return cartRepository.save(newCart);
            });
    }

    @Transactional
    public Cart transferGuestCartToUser(String guestId, User user) {
        Cart guestCart = cartRepository.findByGuestId(guestId)
            .orElse(null);

        if (guestCart == null || guestCart.getItems().isEmpty()) {
            return getCartByUserId(user.getId());
        }

        Cart userCart = getCartByUserId(user.getId());
        
        // Transfer items from guest cart to user cart
        guestCart.getItems().forEach(item -> {
            item.setCart(userCart);
            userCart.getItems().add(item);
        });
        
        userCart.updateTotalAmount();
        
        // Clear and delete guest cart
        guestCart.getItems().clear();
        cartRepository.delete(guestCart);
        
        return cartRepository.save(userCart);
    }
}
