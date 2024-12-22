package com.example.csticaret.service.order;

import com.example.csticaret.model.Order;
import com.example.csticaret.model.Cart;
import com.example.csticaret.request.ShippingDetailsRequest;
import com.example.csticaret.enums.OrderStatus;
import org.springframework.core.io.Resource;

import java.util.List;

public interface IOrderService {
    Order createOrderFromCart(Cart cart, ShippingDetailsRequest shippingDetails);
    Order getOrderById(Long orderId);
    Order processPayment(Long orderId);
    String generateInvoicePdf(Order order);
    Resource getInvoicePdf(Order order);
    List<Order> getUserOrders(Long userId);

    Order cancelOrder(Long orderId, Long id);

    Order refundOrder(Long orderId, Long id);

    List<Order> getAllOrders();
    Order updateOrderStatus(Long id, OrderStatus status);
}
