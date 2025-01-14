package com.example.csticaret.repository;

import com.example.csticaret.model.OrderItem;
import com.example.csticaret.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder_Id(Long orderId);
    
    Optional<OrderItem> findByOrder_IdAndId(Long orderId, Long id);

    List<OrderItem> findByRefundStatusIn(Collection<RefundStatus> statuses);
} 