package com.example.csticaret.repository;

import com.example.csticaret.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteAllByCart_Id(Long cartId);
    List<CartItem> findByCart_Id(Long cartId);
}
