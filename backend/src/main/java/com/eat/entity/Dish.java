package com.eat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dishes")
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 家庭 ID；菜归属家庭共享菜单 */
    @Column(name = "family_id")
    private Long familyId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String image;

    private String category;

    /** 当日主厨 */
    @Column(name = "cook_user_id", length = 64)
    private String cookUserId;

    /** 这道菜是哪天做的（用于历史回顾） */
    @Column(name = "cook_date")
    private LocalDate cookDate;

    private String mealTime; // 早餐、午餐、晚餐

    /** 实际发布人（可能是主厨自己，也可能是其他成员代发） */
    @Column(name = "created_by_user_id", length = 64)
    private String createdByUserId;

    private Boolean available = true;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
