package com.cs308.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "discount")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discountId")
    private Integer discountId;

    @Column(name = "discountValue", precision = 5, scale = 2)
    private Double discountValue;

    @Column(name = "expiryDate")
    private Date expiryDate;

    @Column(name = "isActive")
    private Boolean isActive;

    @Column(name = "discountCode", length = 50, unique = true)
    private String discountCode;

    @Column(name = "startDate")
    private Date startDate;
}

