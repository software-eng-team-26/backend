package com.cs308.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;



@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderId")
    private Long orderId;

    @Column(name = "orderStatus", length = 20)
    private String orderStatus;

    @Column(name = "totalPrice", precision = 10, scale = 2)
    private Double totalPrice;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private Users user;  // Make sure to use your own `User` entity

    @ManyToOne
    @JoinColumn(name = "shippingId", referencedColumnName = "shippingId")
    private Shipping shipping;
}
