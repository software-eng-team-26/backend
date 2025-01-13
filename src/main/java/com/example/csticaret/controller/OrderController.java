package com.example.csticaret.controller;

import com.example.csticaret.model.*;
import com.example.csticaret.request.ShippingDetailsRequest;
import com.example.csticaret.response.ApiResponse;
import com.example.csticaret.service.order.IOrderService;
import com.example.csticaret.service.cart.ICartService;
import com.example.csticaret.service.user.IUserService;
import com.example.csticaret.service.email.EmailService;
import com.example.csticaret.repository.OrderRepository;
import com.example.csticaret.enums.OrderStatus;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.exception.InsufficientStockException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final IOrderService orderService;
    private final ICartService cartService;
    private final IUserService userService;
    private final EmailService emailService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(
            @RequestBody @Valid ShippingDetailsRequest shippingDetails,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("User not authenticated", null));
            }

            User user = userService.getUserByEmail(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("User not found", null));
            }

            Cart cart = cartService.getCartByUserId(user.getId());
            if (cart == null || cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("Cart is empty", null));
            }

            // Check stock availability for all items
            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();
                if (!product.hasStock(item.getQuantity())) {
                    return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(
                            String.format("Insufficient stock for product: %s. Available: %d, Requested: %d",
                                product.getName(), product.getInventory(), item.getQuantity()),
                            null
                        ));
                }
            }

            // Create order
            Order order = orderService.createOrderFromCart(cart, shippingDetails);
            order.setOrderStatus(OrderStatus.PENDING);
            order = orderRepository.save(order);
            
            // Decrease stock
            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();
                product.decreaseStock(item.getQuantity());
                productRepository.save(product);
            }
            
            // Clear the cart
            cartService.clearCart(cart.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("order", order);
            response.put("redirectUrl", "/payment/" + order.getId());
            
            return ResponseEntity.ok(new ApiResponse<>("Order created successfully", response));
        } catch (InsufficientStockException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating order:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to create order", null));
        }
    }
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            Order cancelledOrder = orderService.cancelOrder(orderId, user.getId());
            return ResponseEntity.ok(new ApiResponse<>("Order cancelled successfully", cancelledOrder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error cancelling order:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to cancel order", null));
        }
    }
    @PostMapping("/{orderId}/items/{itemId}/refund")
    public ResponseEntity<ApiResponse<OrderItem>> requestRefund(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            Order order = orderService.getOrderById(orderId);

            // 1. Sipariş tarihi ile bugünün tarihini karşılaştır
            LocalDate orderDate = order.getOrderDate().toLocalDate();
            LocalDate currentDate = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(orderDate, currentDate);

            // 2. 30 günden büyükse refund reddedilir
            if (daysBetween > 30) {
                throw new IllegalArgumentException("Refund requests can only be made within 30 days after delivery.");
            }

            // 3. Refund işlemini gerçekleştir
            OrderItem orderItem = orderService.requestRefund(orderId, itemId, user.getId());
            return ResponseEntity.ok(new ApiResponse<>("Refund request submitted successfully", orderItem));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error submitting refund request:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to submit refund request", null));
        }
    }
    @PostMapping("/{orderId}/items/{itemId}/refund/approve")
    public ResponseEntity<ApiResponse<OrderItem>> approveRefund(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestParam boolean approved) {
        try {
            OrderItem updatedOrderItem = orderService.approveRefund(orderId, itemId, approved);
            String message = approved ? "Refund approved successfully" : "Refund rejected successfully";
            return ResponseEntity.ok(new ApiResponse<>(message, updatedOrderItem));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error approving refund:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to process refund approval", null));
        }
    }









    @PostMapping("/{orderId}/complete-payment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> completePayment(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            Order order = orderService.getOrderById(orderId);
            
            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("You don't have permission to access this order", null));
            }

            // For digital products (courses), mark as DELIVERED immediately after payment
            order.setOrderStatus(OrderStatus.PROCESSING);
            order = orderRepository.save(order);

            // Generate invoice and send email
            try {
                String invoicePdfPath = orderService.generateInvoicePdf(order);
                emailService.sendOrderConfirmation(order.getShippingEmail(), order, invoicePdfPath);
            } catch (Exception e) {
                log.error("Failed to send order confirmation email: {}", e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("order", order);
            
            return ResponseEntity.ok(new ApiResponse<>("Payment completed and course access granted", response));
        } catch (Exception e) {
            log.error("Error completing payment:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to complete payment", null));
        }
    }

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<Resource> getInvoice(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        Order order = orderService.getOrderById(orderId);
        
        if (!order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Resource invoice = orderService.getInvoicePdf(order);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice.pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(invoice);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("User not authenticated", null));
            }
            
            User user = userService.getUserByEmail(userDetails.getUsername());
            List<Order> orders = orderService.getUserOrders(user.getId());
            return ResponseEntity.ok(new ApiResponse<>("Orders retrieved successfully", orders));
        } catch (Exception e) {
            log.error("Error fetching user orders:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to fetch orders", null));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            Order order = orderService.getOrderById(orderId);
            
            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("You don't have permission to view this order", null));
            }
            
            return ResponseEntity.ok(new ApiResponse<>("Order retrieved successfully", order));
        } catch (Exception e) {
            log.error("Error fetching order:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to fetch order", null));
        }
    }

    @PostMapping("/{orderId}/update-status")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        try {
            Order order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(new ApiResponse<>("Order status updated successfully", order));
        } catch (Exception e) {
            log.error("Error updating order status:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to update order status", null));
        }
    }

    @GetMapping("/admin/all")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(new ApiResponse<>("Orders retrieved successfully", orders));
        } catch (Exception e) {
            log.error("Error fetching all orders:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to fetch orders", null));
        }
    }
}




