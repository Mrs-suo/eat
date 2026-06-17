package com.eat.repository;

import com.eat.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);
    List<Order> findByFamilyId(Long familyId);
    Order findByOrderNo(String orderNo);
}
