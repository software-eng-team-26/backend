package com.example.csticaret.service.cart;

import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.Cart;
import com.example.csticaret.model.CartItem;
import com.example.csticaret.model.Product;
import com.example.csticaret.repository.CartItemRepository;
import com.example.csticaret.repository.CartRepository;
import com.example.csticaret.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final IProductService productService;
    private final ICartService cartService;
    
    @Override
    public void addItemToCart(Long cartId, Long productId, int quantity) {
        Cart cart = cartService.getCart(cartId);
        Product product = productService.getProductById(productId);
        
        // Find existing item, with proper null checks
        CartItem existingItem = null;
        for (CartItem item : cart.getItems()) {
            if (item != null && item.getProduct() != null && 
                item.getProduct().getId().equals(productId)) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            // Update existing item
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setUnitPrice(product.getPrice());
            existingItem.calculateTotalPrice();
            cartItemRepository.save(existingItem);
        } else {
            // Create new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            newItem.calculateTotalPrice();
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        cart.updateTotalAmount();
        cartRepository.save(cart);
    }

    @Override
    public void removeItemFromCart(Long cartId, Long productId) {
        Cart cart = cartService.getCart(cartId);
        CartItem itemToRemove = getCartItem(cartId, productId);
        cart.removeItem(itemToRemove);
        cartRepository.save(cart);
    }

    @Override
    public void updateItemQuantity(Long cartId, Long productId, int quantity) {
        Cart cart = cartService.getCart(cartId);
        cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    item.setUnitPrice(item.getProduct().getPrice());
                    item.calculateTotalPrice();
                });
        
        cart.updateTotalAmount();
        cartRepository.save(cart);
    }

    @Override
    public CartItem getCartItem(Long cartId, Long productId) {
        Cart cart = cartService.getCart(cartId);
        return cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }

    @Override
    public List<CartItem> getCartItems(Long cartId) {
        return cartItemRepository.findByCart_Id(cartId);
    }
}
