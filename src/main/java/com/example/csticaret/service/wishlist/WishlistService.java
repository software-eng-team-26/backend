package com.example.csticaret.service.wishlist;

import com.example.csticaret.model.Product;
import com.example.csticaret.model.User;
import com.example.csticaret.model.Wishlist;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.repository.UserRepository;
import com.example.csticaret.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Wishlist getWishlistByUserId(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    public Wishlist addProductToWishlist(Long userId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId);

        if (wishlist == null) {
            wishlist = new Wishlist();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            wishlist.setUser(user); // User'ı setliyoruz.
        }

        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + productId);
        }

        wishlist.getProducts().add(product.get());
        return wishlistRepository.save(wishlist);
    }

    public Wishlist removeProductFromWishlist(Long userId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId);

        if (wishlist == null) {
            throw new RuntimeException("Wishlist not found for user id: " + userId);
        }

        wishlist.getProducts().removeIf(product -> product.getId().equals(productId));
        return wishlistRepository.save(wishlist);
    }
}
