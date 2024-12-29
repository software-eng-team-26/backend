package com.example.csticaret.dto;

import com.example.csticaret.request.RefundRequest;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequestDto {
    private Long id;
    private Long orderId;
    private String productName;
    private int quantity;
    private BigDecimal refundAmount;
    private String status;

    public RefundRequestDto(RefundRequest refundRequest) {
        this.id = refundRequest.getId();
        this.orderId = refundRequest.getOrder().getId();
        this.productName = refundRequest.getProduct().getName();
        this.quantity = refundRequest.getQuantity();
        this.refundAmount = refundRequest.getRefundAmount();
        this.status = refundRequest.getStatus();
    }
}
