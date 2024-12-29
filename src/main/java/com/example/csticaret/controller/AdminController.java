package com.example.csticaret.controller;

import com.example.csticaret.dto.RefundRequestDto;
import com.example.csticaret.service.RefundService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final RefundService refundService;

    
    public AdminController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping("/refunds")
    public ResponseEntity<List<RefundRequestDto>> getRefundRequests() {
        // RefundService'den RefundRequest listesi alınır
        List<RefundRequestDto> refundRequestDtos = refundService.getPendingRefundRequests()
            .stream()
            .map(RefundRequestDto::new) // RefundRequestDto'nun constructor'ı kullanılarak dönüşüm yapılır
            .toList();

        return ResponseEntity.ok(refundRequestDtos);
    }
}
