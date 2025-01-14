package com.example.csticaret.controller;

import com.example.csticaret.model.Discount;
import com.example.csticaret.request.DiscountRequest;
import com.example.csticaret.service.discount.DiscountService;
import com.example.csticaret.response.ApiResponse;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/discounts")
@RequiredArgsConstructor
public class DiscountController {
    private final DiscountService discountService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Discount>> createDiscount(@RequestBody DiscountRequest request) {
        try {
            Discount discount = discountService.createDiscount(
                request.getProductId(),
                request.getDiscountRate()
            );
            return ResponseEntity.ok(new ApiResponse<>("Discount created successfully", discount));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to create discount: " + e.getMessage(), null));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Discount>>> getAllDiscounts() {
        try {
            List<Discount> discounts = discountService.getAllDiscounts();
            return ResponseEntity.ok(new ApiResponse<>("Discounts retrieved successfully", discounts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to fetch discounts", null));
        }
    }

    @PostMapping("/{discountId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateDiscount(@PathVariable Long discountId) {
        try {
            discountService.deactivateDiscount(discountId);
            return ResponseEntity.ok(new ApiResponse<>("Discount deactivated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to deactivate discount", null));
        }
    }
} 