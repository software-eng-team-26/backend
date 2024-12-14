package com.example.csticaret.enums;

public enum OrderStatus {
    PENDING,        // Initial state when order is created
    PAID,
    PROCESSING,     // Payment is being processed
    PROVISIONING,   // Digital product is being set up (like course access)
    DELIVERED,// Access granted to digital content
    CANCELLED,
}
