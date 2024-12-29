package com.example.csticaret.service;

import com.example.csticaret.request.RefundRequest;
import com.example.csticaret.repository.RefundRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RefundService {

    private final RefundRepository refundRepository;

    
    public RefundService(RefundRepository refundRepository) {
        this.refundRepository = refundRepository;
    }

    public List<RefundRequest> getPendingRefundRequests() {
        return refundRepository.findByStatus("PENDING");
    }
}
