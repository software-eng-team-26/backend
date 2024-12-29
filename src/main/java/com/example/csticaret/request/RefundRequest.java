package com.example.csticaret.request;


import jakarta.persistence.*;
import lombok.Data;
import com.example.csticaret.model.Product;
import com.example.csticaret.model.Order;



import java.math.BigDecimal;

@Entity
@Data
@Table(name = "refund_requests")
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;
    private BigDecimal refundAmount;

    @Column(name = "status")
    private String status; // "PENDING", "APPROVED", "REJECTED" gibi
}
