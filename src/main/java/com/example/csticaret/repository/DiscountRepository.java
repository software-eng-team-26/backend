package com.example.csticaret.repository;

import com.example.csticaret.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    List<Discount> findByProductId(Long productId);
    List<Discount> findByIsActiveTrue();
    Optional<Discount> findByProductIdAndIsActiveTrue(Long productId);
} 