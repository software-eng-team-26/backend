package com.example.csticaret.repository;

import com.example.csticaret.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByProductIdAndApprovedTrue(Long productId); // Approved comments for a product
}
