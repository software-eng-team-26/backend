package com.example.csticaret.Controller.Order;

import com.example.csticaret.controller.OrderController;
import com.example.csticaret.enums.OrderStatus;
import com.example.csticaret.model.Cart;
import com.example.csticaret.model.Order;
import com.example.csticaret.model.User;
import com.example.csticaret.request.ShippingDetailsRequest;
import com.example.csticaret.response.ApiResponse;
import com.example.csticaret.service.cart.ICartService;
import com.example.csticaret.service.order.IOrderService;
import com.example.csticaret.service.user.IUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collections;
import java.util.List;
import java.util.Map;




class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private IOrderService orderService;

    @Mock
    private ICartService cartService;

    @Mock
    private IUserService userService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        ShippingDetailsRequest shippingDetails = new ShippingDetailsRequest();
        shippingDetails.setAddress("123 Test Street");
        shippingDetails.setEmail("test@example.com");
        shippingDetails.setPhone("1234567890");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        Cart mockCart = new Cart();
        mockCart.setUser(mockUser);
        mockCart.setItems(Collections.emptySet());

        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setOrderStatus(OrderStatus.PENDING);

        Mockito.when(userDetails.getUsername()).thenReturn("test@example.com");
        Mockito.when(userService.getUserByEmail("test@example.com")).thenReturn(mockUser);
        Mockito.when(cartService.getCartByUserId(1L)).thenReturn(mockCart);
        Mockito.when(orderService.createOrderFromCart(mockCart, shippingDetails)).thenReturn(mockOrder);

        // Act
        ResponseEntity<ApiResponse<Map<String, Object>>> response = orderController.createOrder(shippingDetails, userDetails);

        // Assert
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Order created successfully", response.getBody().getMessage());
        Mockito.verify(orderService, Mockito.times(1)).createOrderFromCart(mockCart, shippingDetails);
    }

    @Test
    void testCreateOrder_UserNotAuthenticated() {
        // Arrange
        ShippingDetailsRequest shippingDetails = new ShippingDetailsRequest();

        // Act
        ResponseEntity<ApiResponse<Map<String, Object>>> response = orderController.createOrder(shippingDetails, null);

        // Assert
        Assertions.assertEquals(401, response.getStatusCodeValue());
        Assertions.assertEquals("User not authenticated", response.getBody().getMessage());
    }

    @Test
    void testCancelOrder_Success() {
        // Arrange
        Long orderId = 1L;
        User mockUser = new User();
        mockUser.setId(1L);

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setOrderStatus(OrderStatus.CANCELLED);

        Mockito.when(userDetails.getUsername()).thenReturn("test@example.com");
        Mockito.when(userService.getUserByEmail("test@example.com")).thenReturn(mockUser);
        Mockito.when(orderService.cancelOrder(orderId, 1L)).thenReturn(mockOrder);

        // Act
        ResponseEntity<ApiResponse<Order>> response = orderController.cancelOrder(orderId, userDetails);

        // Assert
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Order cancelled successfully", response.getBody().getMessage());
        Assertions.assertEquals(mockOrder, response.getBody().getData());
        Mockito.verify(orderService, Mockito.times(1)).cancelOrder(orderId, 1L);
    }

    @Test
    void testRefundOrder_Success() {
        // Arrange
        Long orderId = 1L;
        User mockUser = new User();
        mockUser.setId(1L);

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setOrderStatus(OrderStatus.CANCELLED);

        Mockito.when(userDetails.getUsername()).thenReturn("test@example.com");
        Mockito.when(userService.getUserByEmail("test@example.com")).thenReturn(mockUser);
        Mockito.when(orderService.refundOrder(orderId, 1L)).thenReturn(mockOrder);

        // Act
        ResponseEntity<ApiResponse<Order>> response = orderController.refundOrder(orderId, userDetails);

        // Assert
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Order refunded successfully", response.getBody().getMessage());
        Assertions.assertEquals(mockOrder, response.getBody().getData());
        Mockito.verify(orderService, Mockito.times(1)).refundOrder(orderId, 1L);
    }

    @Test
    void testGetUserOrders_Success() {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);

        Order mockOrder = new Order();
        mockOrder.setId(1L);

        Mockito.when(userDetails.getUsername()).thenReturn("test@example.com");
        Mockito.when(userService.getUserByEmail("test@example.com")).thenReturn(mockUser);
        Mockito.when(orderService.getUserOrders(1L)).thenReturn(List.of(mockOrder));

        // Act
        ResponseEntity<ApiResponse<List<Order>>> response = orderController.getUserOrders(userDetails);

        // Assert
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Orders retrieved successfully", response.getBody().getMessage());
        Assertions.assertEquals(1, response.getBody().getData().size());
        Mockito.verify(orderService, Mockito.times(1)).getUserOrders(1L);
    }

    @Test
    void testGetOrder_Success() {
        // Arrange
        Long orderId = 1L;
        User mockUser = new User();
        mockUser.setId(1L);

        Order mockOrder = new Order();
        mockOrder.setId(orderId);

        Mockito.when(userDetails.getUsername()).thenReturn("test@example.com");
        Mockito.when(userService.getUserByEmail("test@example.com")).thenReturn(mockUser);
        Mockito.when(orderService.getOrderById(orderId)).thenReturn(mockOrder);

        // Act
        ResponseEntity<ApiResponse<Order>> response = orderController.getOrder(orderId, userDetails);

        // Assert
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Order retrieved successfully", response.getBody().getMessage());
        Assertions.assertEquals(mockOrder, response.getBody().getData());
        Mockito.verify(orderService, Mockito.times(1)).getOrderById(orderId);
    }
}