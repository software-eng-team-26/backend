package com.example.csticaret.controller;

import com.example.csticaret.model.Wishlist;
import com.example.csticaret.service.wishlist.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/{userId}")
    public ResponseEntity<Wishlist> getWishlist(@PathVariable Long userId) {
        Wishlist wishlist = wishlistService.getWishlistByUserId(userId);
        if (wishlist == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/{userId}/add/{productId}")
    public ResponseEntity<Wishlist> addProductToWishlist(@PathVariable Long userId, @PathVariable Long productId) {
        Wishlist wishlist = wishlistService.addProductToWishlist(userId, productId);
        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/{userId}/remove/{productId}")
    public ResponseEntity<Wishlist> removeProductFromWishlist(@PathVariable Long userId, @PathVariable Long productId) {
        Wishlist wishlist = wishlistService.removeProductFromWishlist(userId, productId);
        return ResponseEntity.ok(wishlist);
    }
}

