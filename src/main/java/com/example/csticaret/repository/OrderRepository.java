package com.example.csticaret.repository;

import com.example.csticaret.model.Order;
import com.example.csticaret.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o " +
           "JOIN o.items oi " +
           "WHERE o.user.id = :userId " +
           "AND oi.product.id = :productId " +
           "AND o.orderStatus = :status")
    boolean existsByUserIdAndProductIdAndStatus(
        @Param("userId") Long userId,
        @Param("productId") Long productId,
        @Param("status") OrderStatus status
    );
}


