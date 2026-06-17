package com.eat.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dish_edit_requests")
public class DishEditRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id")
    private Long familyId;

    private String userId;

    // Associated original dish. Empty when the request creates a new dish.
    @ManyToOne
    @JoinColumn(name = "original_dish_id")
    private Dish originalDish;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String image;

    private String category;

    private String mealTime;

    private Integer status = 0;
    private String rejectReason;

    private LocalDateTime createTime = LocalDateTime.now();

    private LocalDateTime updateTime;
}
