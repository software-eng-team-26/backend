package com.cs308.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentId")
    private Long paymentId;

    @Column(name = "paymentType", length = 20)
    private String paymentType;

    @Column(name = "paymentStatus", length = 20)
    private String paymentStatus;

    @Column(name = "paymentRefund", length = 20)
    private String paymentRefund;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "paymentDate")
    private Date paymentDate;

    @ManyToOne
    @JoinColumn(name = "orderId", referencedColumnName = "orderId")
    private Order order;
}
