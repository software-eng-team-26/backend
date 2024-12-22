package com.example.csticaret.dto;

import com.example.csticaret.enums.OrderStatus;
import com.example.csticaret.model.Order;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String shippingEmail;
    private String shippingPhone;

    public static OrderDTO fromOrder(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUserName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
        }
        dto.setOrderDate(order.getOrderDate());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingEmail(order.getShippingEmail());
        dto.setShippingPhone(order.getShippingPhone());
        return dto;
    }
}
