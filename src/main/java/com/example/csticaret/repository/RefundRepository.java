package com.example.csticaret.repository;

import com.example.csticaret.request.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<RefundRequest, Long> {
    List<RefundRequest> findByStatus(String status);
}
