package com.example.csticaret.Controller.Order;


import com.example.csticaret.enums.OrderStatus;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.*;
import com.example.csticaret.repository.OrderRepository;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.request.ShippingDetailsRequest;
import com.example.csticaret.service.cart.ICartService;
import com.example.csticaret.service.email.EmailService;
import com.example.csticaret.service.order.OrderService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ICartService cartService;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrderFromCart_Success() {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);

        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setInventory(10);
        mockProduct.setPrice(new BigDecimal("100.00"));

        CartItem mockCartItem = new CartItem();
        mockCartItem.setProduct(mockProduct);
        mockCartItem.setQuantity(2);

        Cart mockCart = new Cart();
        mockCart.setUser(mockUser);
        mockCart.setTotalAmount(new BigDecimal("200.00"));
        mockCart.setItems(Set.of(mockCartItem)); // Use CartItem instead of OrderItem

        ShippingDetailsRequest shippingDetails = new ShippingDetailsRequest();
        shippingDetails.setAddress("123 Test Street");
        shippingDetails.setEmail("test@example.com");
        shippingDetails.setPhone("1234567890");

        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        Order createdOrder = orderService.createOrderFromCart(mockCart, shippingDetails);

        // Assert
        assertNotNull(createdOrder);
        assertEquals(OrderStatus.PENDING, createdOrder.getOrderStatus());
        verify(orderRepository, times(2)).save(any(Order.class)); // Saved twice: once before adding items, once after
    }



    @Test
    void testGetOrderById_Success() {
        // Arrange
        Long orderId = 1L;
        Order mockOrder = new Order();
        mockOrder.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act
        Order result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void testGetOrderById_NotFound() {
        // Arrange
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(orderId));
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void testCancelOrder_Success() {
        // Arrange
        Long orderId = 1L;
        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(userId);

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setUser(mockUser);
        mockOrder.setOrderStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        Order canceledOrder = orderService.cancelOrder(orderId, userId);

        // Assert
        assertNotNull(canceledOrder);
        assertEquals(OrderStatus.CANCELLED, canceledOrder.getOrderStatus());
        verify(orderRepository, times(1)).save(mockOrder);
    }

    @Test
    void testCancelOrder_UserMismatch() {
        // Arrange
        Long orderId = 1L;
        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(2L); // Different user ID

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setUser(mockUser);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> orderService.cancelOrder(orderId, userId));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testRefundOrder_Success() {
        // Arrange
        Long orderId = 1L;
        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(userId);

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setUser(mockUser);
        mockOrder.setOrderStatus(OrderStatus.DELIVERED);
        mockOrder.setOrderDate(LocalDateTime.now().minusDays(10)); // Within 30-day refund window

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        Order refundedOrder = orderService.refundOrder(orderId, userId);

        // Assert
        assertNotNull(refundedOrder);
        assertEquals(OrderStatus.CANCELLED, refundedOrder.getOrderStatus());
        verify(orderRepository, times(1)).save(mockOrder);
    }

    @Test
    void testRefundOrder_ExpiredRefundPeriod() {
        // Arrange
        Long orderId = 1L;
        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(userId);

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setUser(mockUser);
        mockOrder.setOrderStatus(OrderStatus.DELIVERED);
        mockOrder.setOrderDate(LocalDateTime.now().minusDays(40)); // Beyond 30-day refund window

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> orderService.refundOrder(orderId, userId));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetUserOrders_Success() {
        // Arrange
        Long userId = 1L;
        Order mockOrder = new Order();
        mockOrder.setUser(new User());
        mockOrder.getUser().setId(userId);

        List<Order> mockOrders = List.of(mockOrder);
        when(orderRepository.findByUserId(userId)).thenReturn(mockOrders);

        // Act
        List<Order> result = orderService.getUserOrders(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testCreateOrderFromCart_InsufficientInventory() {
        // Arrange
        Cart mockCart = getCart();

        ShippingDetailsRequest shippingDetails = new ShippingDetailsRequest();
        shippingDetails.setAddress("123 Test Street");
        shippingDetails.setEmail("test@example.com");
        shippingDetails.setPhone("1234567890");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> orderService.createOrderFromCart(mockCart, shippingDetails));
        verify(orderRepository, never()).save(any(Order.class)); // Ensure no order is saved
    }

    @NotNull
    private static Cart getCart() {
        User mockUser = new User();
        mockUser.setId(1L);

        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setInventory(1); // Insufficient inventory
        mockProduct.setPrice(new BigDecimal("100.00"));

        CartItem mockCartItem = new CartItem();
        mockCartItem.setProduct(mockProduct);
        mockCartItem.setQuantity(2); // Requested quantity exceeds inventory

        Cart mockCart = new Cart();
        mockCart.setUser(mockUser);
        mockCart.setTotalAmount(new BigDecimal("200.00"));
        mockCart.setItems(Set.of(mockCartItem));
        return mockCart;
    }

}



