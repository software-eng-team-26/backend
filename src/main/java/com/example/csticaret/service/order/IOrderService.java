package com.example.csticaret.service.order;

import com.example.csticaret.dto.OrderDto;
import com.example.csticaret.model.Order;

import java.util.List;

public interface IOrderService {
    Order placeOrder(Long userId);
    OrderDto getOrder(Long orderId);
    List<OrderDto> getUserOrders(Long userId);
}
