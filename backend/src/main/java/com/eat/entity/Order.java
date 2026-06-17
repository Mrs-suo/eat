package com.eat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNo;

    @Column(name = "family_id")
    private Long familyId;

    private String userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalPrice;

    private Integer status = 0; // 0-待支付 1-已支付 2-制作中 3-已完成

    private String address;

    private String phone;

    private LocalDateTime createTime = LocalDateTime.now();

    private LocalDateTime updateTime;
}
