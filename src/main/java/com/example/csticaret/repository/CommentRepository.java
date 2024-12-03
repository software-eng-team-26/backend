package com.example.csticaret.repository;

import com.example.csticaret.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByProductIdAndApprovedTrue(Long productId);
    
    @Query("SELECT c FROM Comment c WHERE c.product.id = :productId AND c.approved = true AND c.rating IS NOT NULL")
    List<Comment> findApprovedRatingsByProductId(@Param("productId") Long productId);
}