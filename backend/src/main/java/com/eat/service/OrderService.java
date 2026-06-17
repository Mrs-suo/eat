package com.eat.service;

import com.eat.entity.Order;
import com.eat.entity.OrderItem;
import com.eat.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByFamily(Long familyId) {
        if (familyId == null) return List.of();
        return orderRepository.findByFamilyId(familyId);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(Order order) {
        if (order.getFamilyId() == null) {
            throw new IllegalArgumentException("缺少家庭信息");
        }
        order.setOrderNo(generateOrderNo());
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
                if (item.getPrice() != null && item.getQuantity() != null) {
                    total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }
        order.setTotalPrice(total);

        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long id, Integer status) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(status);
            order.setUpdateTime(LocalDateTime.now());
            return orderRepository.save(order);
        }
        return null;
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
