package com.example.csticaret.repository;

import com.example.csticaret.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    
    Optional<OrderItem> findByOrderIdAndId(Long orderId, Long id);
} 