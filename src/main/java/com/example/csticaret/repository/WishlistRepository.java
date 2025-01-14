package com.example.csticaret.repository;

import com.example.csticaret.model.Product;
import com.example.csticaret.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Wishlist findByUserId(Long userId);
    List<Wishlist> findByProductsContaining(Product product);
}
