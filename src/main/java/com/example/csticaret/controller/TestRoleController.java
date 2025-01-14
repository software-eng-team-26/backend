package com.example.csticaret.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestRoleController {

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'ADMIN')")
    public ResponseEntity<String> salesManagerEndpoint() {
        return ResponseEntity.ok("Sales Manager Access Granted");
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('PRODUCT_MANAGER', 'ADMIN')")
    public ResponseEntity<String> productManagerEndpoint() {
        return ResponseEntity.ok("Product Manager Access Granted");
    }
} 