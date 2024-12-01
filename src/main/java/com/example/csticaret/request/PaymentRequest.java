package com.example.csticaret.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank(message = "Card number is required")
    private String cardNumber;
    
    @NotBlank(message = "Expiry date is required")
    private String expiryDate;
    
    @NotBlank(message = "CVV is required")
    private String cvv;
    
    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;
} 