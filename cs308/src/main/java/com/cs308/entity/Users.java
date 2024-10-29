package com.cs308.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;

    @Column(name = "firstName", length = 50, nullable = false)
    private String firstName;

    @Column(name = "lastName", length = 50, nullable = false)
    private String lastName;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "phoneNumber", length = 15)
    private String phoneNumber;
}

