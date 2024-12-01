package com.example.csticaret.controller;

import com.example.csticaret.model.Order;
import com.example.csticaret.model.Cart;
import com.example.csticaret.model.User;
import com.example.csticaret.request.ShippingDetailsRequest;
import com.example.csticaret.response.ApiResponse;
import com.example.csticaret.service.order.IOrderService;
import com.example.csticaret.service.cart.ICartService;
import com.example.csticaret.service.user.IUserService;
import com.example.csticaret.service.email.EmailService;
import com.example.csticaret.repository.OrderRepository;
import com.example.csticaret.enums.OrderStatus;
import com.example.csticaret.model.Product;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.exception.InsufficientStockException;
import com.example.csticaret.model.CartItem;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
            User user = userService.getUserByEmail(userDetails.getUsername());
            Cart cart = cartService.getCartByUserId(user.getId());
            
            if (cart.getItems().isEmpty()) {
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
            response.put("redirectUrl", "/payment/" + order.getOrderId());
            
            return ResponseEntity.ok(new ApiResponse<>("Order created successfully", response));
        } catch (Exception e) {
            log.error("Error creating order:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to create order", null));
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

            // Mark order as paid
            order.setOrderStatus(OrderStatus.PAID);
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
            
            return ResponseEntity.ok(new ApiResponse<>("Payment completed successfully", response));
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
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Order order = orderService.getOrderById(orderId);
            order.setOrderStatus(status);
            order = orderRepository.save(order);
            
            return ResponseEntity.ok(new ApiResponse<>("Order status updated successfully", order));
        } catch (Exception e) {
            log.error("Error updating order status:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Failed to update order status", null));
        }
    }
}




